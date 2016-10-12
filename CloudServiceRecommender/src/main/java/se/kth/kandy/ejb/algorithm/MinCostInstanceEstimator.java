package se.kth.kandy.ejb.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.log4j.Logger;
import se.kth.kandy.cloud.amazon.Ec2ApiWrapper;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.jpa.AwsEc2InstancePriceFacade;
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;

/**
 * Based on empirical data (Spot history prices) estimates the reliability of an instance for the user requested time.
 * Also bid is estimated to meet market price and reliability lower bound
 *
 * @author Hossein
 */
@Stateless
public class MinCostInstanceEstimator {

  private static final Logger logger = Logger.getLogger(MinCostInstanceEstimator.class);
  private static final long ONE_HOUR_MILISECOND = 3600000;

  @EJB
  private SpotInstanceStatistics spotInstanceStatistics;
  @EJB
  private InstanceFilter instanceFilter;
  @EJB
  private AwsEc2InstancePriceFacade awsEc2InstancePriceFacade;
  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;

  /**
   * Estimate the probability that specified instance will be available for the user request time regarding the
   * specified bid and availability zone.
   *
   * Uses Kaplan-Meier estimator to calculate empirical probability of success on a sorted list of samples availability
   * time. samples are from past 85 days.
   *
   * @param instanceType
   * @param availabilityZone
   * @param bid
   * @param availabilityTime in millisecond
   * @return empirical probability of success between [0,1] - S(r,v)
   */
  public float estimateSpotReliability(String instanceType, String availabilityZone, BigDecimal bid,
      long availabilityTime) {

    List<Long> instancesAvailTimeList = spotInstanceStatistics.getSpotSamplesAvailabilityTime(instanceType,
        availabilityZone, bid);

    float probabilityOfSuccess = 1; // if there would be no termination
    if (instancesAvailTimeList.isEmpty()) {
      // no recent spot price data found for the instance/zone. zone or instance is deprecated
      probabilityOfSuccess = -1;
    } else {

      // calcualte empirical probability of success with Kaplan-Meier estimator
      for (int i = 0; (i < instancesAvailTimeList.size() - 1)
          /* minus one, because there is 0 instances left after the last item in the list, which result the equation
           to zero*/
          && (instancesAvailTimeList.get(i) <= availabilityTime); i++) {
        // if there would be termination
        int NxV = instancesAvailTimeList.size() - i - 1;
        probabilityOfSuccess *= ((float) (NxV - 1)) / NxV;
      }
    }
    logger.debug(
        "Reliability: " + probabilityOfSuccess + " for Instance: " + instanceType + " / " + availabilityZone
        + " AvailabilityTime: " + availabilityTime + " Bid: " + bid);
    return probabilityOfSuccess;
  }

  /**
   * Based on empirical spot history estimates a bid for the specified instance and zone.
   *
   * Estimated Bid is higher than current spot market price and also with a probability greater than the reliability
   * lower bound, instance would be available for the user requested time.
   *
   * @param instanceType
   * @param availabilityZone
   * @param availabilityTime in millisecond
   * @param reliabilityLowerBound between [0,1] Slb
   * @return bid $/h - return 0 if instance is deprecated
   * @throws ServiceRecommanderException
   */
  public BigDecimal estimateMinBid(String instanceType, String availabilityZone, long availabilityTime,
      float reliabilityLowerBound) throws ServiceRecommanderException {
    // starts the bid from current spot market price
    BigDecimal bid = Ec2ApiWrapper.getInstance().getCurrentLinuxSpotPrice(instanceType, availabilityZone);

    if (bid.compareTo(BigDecimal.ZERO) == 1) {
      //bid should be more than 0, otherwise this instance/zone is not valid any more
      float reliability = estimateSpotReliability(instanceType, availabilityZone, bid, availabilityTime);
      if (reliability != -1) {
        while (reliability < reliabilityLowerBound) {
          bid = bid.add(new BigDecimal("0.02")); // increase the bid in $
          reliability = estimateSpotReliability(instanceType, availabilityZone, bid, availabilityTime);
        }
      }
    }

    return bid; //return 0 if instance or zone deprecated
  }

  /**
   * By using Monte-Carlo simulation, estimates average runtime in the event of failure for spot instances.
   *
   * Failure means availability time of the instance is lower than availabilityTime, there are samples that terminate,
   * but they have exceed availabilityTime before their termination, those are not our target.
   *
   * @param instanceType
   * @param availabilityTime
   * @param bid
   * @param availabilityZone
   * @return average run time hour
   */
  public double estimateTerminationAverageRunTime(String instanceType, String availabilityZone, BigDecimal bid,
      long availabilityTime) {

    List<Long> instancesAvailTimeList = spotInstanceStatistics.getSpotSamplesAvailabilityTime(instanceType,
        availabilityZone, bid);  // ascending list of samples availability time

    long totalRunTimeHours = 0;
    int i = 0;
    for (; (i < instancesAvailTimeList.size()) && (instancesAvailTimeList.get(i) < availabilityTime); i++) {
      totalRunTimeHours += (long) (instancesAvailTimeList.get(i) / ONE_HOUR_MILISECOND);
    }
    if (i == 0) {
      return 0; // there was no termination, it means Srv=1
    }
    double averageRunTimeHours = (double) totalRunTimeHours / (double) i;
    logger.debug(
        "Average run time before termination (hours): " + averageRunTimeHours + " for Instance: " + instanceType + " / "
        + availabilityZone + " AvailabilityTime: " + availabilityTime + " Bid: " + bid);
    return averageRunTimeHours;
  }

  /**
   * Estimates an instance cost (Ondemand/spot), based on the specified parameters.
   *
   * In case of spot Instance it takes in to consideration both success and failure. If instance is ondemand, bid=0,
   * Srv=1, Pmkt should be fetched from database
   *
   * this is not the true cost, but estimated cost to calculate the profitability of an instance in satisfying a
   * request.
   *
   * Estimated Crv = Pmkt * { Srv * ceiling(Tr) + (1-Srv) * floor(Tfavg) }
   *
   * @param instanceType
   * @param zone - Region in case of Ondemand or AvailabilityZone in case of spot
   * @param bid - 0 means Ondemand instance
   * @param availabilityTime
   * @return instance cost in dollar
   * @throws ServiceRecommanderException
   */
  public BigDecimal estimateInstanceCost(String instanceType, String zone, BigDecimal bid,
      long availabilityTime) throws ServiceRecommanderException {

    BigDecimal Pmkt;
    float Srv;
    double Tfavg = 0;
    if (bid.compareTo(BigDecimal.ZERO) == 0) { // Ondemand instance
      Pmkt = awsEc2InstancePriceFacade.getPrice(zone, instanceType);
      Srv = 1;
    } else {//spot instance
      Pmkt = Ec2ApiWrapper.getInstance().getCurrentLinuxSpotPrice(instanceType,
          zone);
      Srv = estimateSpotReliability(instanceType, zone, bid, availabilityTime);
      //Instance average availability time, In case of failure
      Tfavg = estimateTerminationAverageRunTime(instanceType, zone, bid, availabilityTime);
    }

    // Instance availability time, round up in case of success for spot or Ondemand
    long Ts = (availabilityTime / ONE_HOUR_MILISECOND) + 1;

    double time = (Srv * Ts) + ((1 - Srv) * Tfavg);
    BigDecimal Crv = Pmkt.multiply(new BigDecimal(time));

    logger.debug("Estimated cost: " + Crv + " for Instance: " + instanceType + " / "
        + zone + " AvailabilityTime: " + availabilityTime + " Bid: " + bid);
    return Crv;
  }

  /**
   * Filter out EC2 instances with minimum requirement of ECU, memory and storage. For both spot and On-demand type of
   * the instance goes through all available regions and availability zones, calculates the estimated cost regarding the
   * specified availabilityTime and reliabilityLowerBound, and returns the list sorted ascending of instances.
   *
   * @param availabilityTime
   * @param reliabilityLowerBound
   * @param minECU
   * @param minMemoryGB
   * @param minStorage
   * @return ascending sorted list of estimated cost of all filtered instances and their possible zones
   * @throws se.kth.kandy.cloud.common.exception.ServiceRecommanderException
   */
  public List<Ec2Instance> findAllInstancesZonesCost(long availabilityTime, float reliabilityLowerBound,
      InstanceFilter.ECU minECU, float minMemoryGB, InstanceFilter.STORAGEGB minStorage)
      throws ServiceRecommanderException {

    List<String> filteredInstances = instanceFilter.filterEc2InstanceTypes(minECU, minMemoryGB, minStorage);
    List<Ec2Instance> instancesZonesCostList = new ArrayList<>();

    for (String instanceType : filteredInstances) {
      instancesZonesCostList = estimateInstanceZonesCost(availabilityTime, reliabilityLowerBound, instanceType);
    }

    Collections.sort(instancesZonesCostList); // sort the list ascending
    logger.
        debug("Filtered Ec2 instances list. Slb: " + reliabilityLowerBound + " AvailabilityTime: " + availabilityTime);
    for (Ec2Instance instanceCost : instancesZonesCostList) {
      logger.debug(instanceCost);
    }
    return instancesZonesCostList;
  }

  /**
   * For both spot and On-demand type of the instance goes through all available regions and availability zones,
   * calculates the estimated cost regarding the specified availabilityTime and reliabilityLowerBound, and returns the
   * list sorted ascending of instance cost.
   *
   * @param availabilityTime
   * @param reliabilityLowerBound
   * @param instanceType
   * @return ascending sorted list of estimated cost of the instance and all possible zones
   * @throws ServiceRecommanderException
   */
  public List<Ec2Instance> findInstanceZonesCost(long availabilityTime, float reliabilityLowerBound,
      String instanceType) throws ServiceRecommanderException {

    List<Ec2Instance> instanceZonesCostList = estimateInstanceZonesCost(availabilityTime, reliabilityLowerBound,
        instanceType);

    Collections.sort(instanceZonesCostList); // sort the list ascending
    logger.
        debug("Filtered Ec2 instances list. Slb: " + reliabilityLowerBound + " AvailabilityTime: " + availabilityTime);
    for (Ec2Instance instanceCost : instanceZonesCostList) {
      logger.debug(instanceCost);
    }
    return instanceZonesCostList;
  }

  private List<Ec2Instance> estimateInstanceZonesCost(long availabilityTime, float reliabilityLowerBound,
      String instanceType) throws ServiceRecommanderException {

    List<Ec2Instance> instanceZonesCostList = new ArrayList<>();

    List<String> regions = awsEc2InstancePriceFacade.getRegions(instanceType);
    for (String region : regions) { // ondemand
      BigDecimal cost = estimateInstanceCost(instanceType, region, BigDecimal.ZERO, availabilityTime).setScale(
          4, RoundingMode.HALF_UP);
      instanceZonesCostList.add(new Ec2Instance(instanceType, region, cost, Ec2Instance.INSTANCETYPE.ONDEMAND,
          awsEc2InstancePriceFacade.getPrice(region, instanceType)));
    }

    List<String> availabilityZones = awsEc2SpotInstanceFacade.getAvailabilityZones(instanceType);
    for (String availabilityZone : availabilityZones) { // spot
      BigDecimal bid = estimateMinBid(instanceType, availabilityZone, availabilityTime, reliabilityLowerBound);
      if (bid.compareTo(BigDecimal.ZERO) == 0) { // Instance/zone is deprecated
        continue;
      }
      BigDecimal estimatedCost = estimateInstanceCost(instanceType, availabilityZone, bid, availabilityTime).setScale(
          4, RoundingMode.HALF_UP);
      instanceZonesCostList.add(new Ec2Instance(instanceType, availabilityZone, estimatedCost,
          Ec2Instance.INSTANCETYPE.SPOT, bid));
    }

    return instanceZonesCostList;
  }

}
