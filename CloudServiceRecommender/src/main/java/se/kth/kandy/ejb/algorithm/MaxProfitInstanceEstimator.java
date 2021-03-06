package se.kth.kandy.ejb.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
public class MaxProfitInstanceEstimator {

  private static final Logger logger = Logger.getLogger(MaxProfitInstanceEstimator.class);
  private static final long ONE_HOUR_MILISECOND = 3600000;

  @EJB
  private SpotInstanceStatistics spotInstanceStatistics;
  @EJB
  private InstanceFilter instanceFilter;
  @EJB
  private AwsEc2InstancePriceFacade awsEc2InstancePriceFacade;
  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;

  private Ec2ApiWrapper ec2ApiWrapper;

  public MaxProfitInstanceEstimator() throws ServiceRecommanderException {
    this.ec2ApiWrapper = Ec2ApiWrapper.getInstance();
  }

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
   * @param experimentDate
   * @return empirical probability of success between [0,1] - S(r,v)
   */
  public float estimateSpotReliability(String instanceType, String availabilityZone, BigDecimal bid,
      long availabilityTime, Date experimentDate) {

    List<Long> instancesAvailTimeList = spotInstanceStatistics.getSpotSamplesAvailabilityTime(instanceType,
        availabilityZone, bid, availabilityTime, experimentDate); // availability time is just for creating the chart

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
   * @param experimentDate
   * @return bid $/h - return 0 if instance is deprecated
   * @throws ServiceRecommanderException
   */
  public BigDecimal estimateMinBid(String instanceType, String availabilityZone, long availabilityTime,
      float reliabilityLowerBound, Date experimentDate) throws ServiceRecommanderException {
    // starts the bid from current spot market price
    BigDecimal bid = getLinuxSpotPrice(instanceType, availabilityZone, experimentDate);

    String bidIncrement;  // to improve the speed
    if (reliabilityLowerBound == 1) {
      bidIncrement = "0.5";
    } else if (reliabilityLowerBound >= 0.8f) {
      bidIncrement = "0.2";
    } else {
      bidIncrement = "0.1";
    }

    if (bid.compareTo(BigDecimal.ZERO) == 1) {
      //bid should be more than 0, otherwise this instance/zone is not valid any more
      float reliability = estimateSpotReliability(instanceType, availabilityZone, bid, availabilityTime,
          experimentDate);
      if (reliability != -1) {
        while (reliability < reliabilityLowerBound) {
          bid = bid.add(new BigDecimal(bidIncrement)); // increase the bid in $
          reliability = estimateSpotReliability(instanceType, availabilityZone, bid, availabilityTime, experimentDate);
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
   * @param experimentDate
   * @return average run time hour
   */
  public double estimateTerminationAverageRunTime(String instanceType, String availabilityZone, BigDecimal bid,
      long availabilityTime, Date experimentDate) {

    List<Long> instancesAvailTimeList = spotInstanceStatistics.getSpotSamplesAvailabilityTime(instanceType,
        availabilityZone, bid, availabilityTime, experimentDate);  // ascending list of samples availability time

    // availability time is just for creating the chart
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
   * Estimates an instance profit (Ondemand/spot), based on the specified parameters.
   *
   * In case of spot Instance it takes in to consideration both success and failure. If instance is ondemand, bid=0,
   * Srv=1, Pmkt should be fetched from database
   *
   * this is not the true profit, but estimated profit which shows the profitability of an instance in satisfying a
   * request.
   *
   * Estimated Profit = Pmkt * { Srv * ceiling(Tr) + (1-Srv) * floor(Tfavg) }
   *
   * @param instanceType
   * @param zone - Region in case of Ondemand or AvailabilityZone in case of spot
   * @param bid - 0 means Ondemand instance
   * @param availabilityTime
   * @param experimentDate
   * @return instance cost in dollar
   * @throws ServiceRecommanderException
   */
  public BigDecimal estimateInstanceProfit(String instanceType, String zone, BigDecimal bid,
      long availabilityTime, Date experimentDate) throws ServiceRecommanderException {

    BigDecimal Pmkt;
    float Srv;
    double Tfavg = 0;
    if (bid.compareTo(BigDecimal.ZERO) == 0) { // Ondemand instance
      Pmkt = awsEc2InstancePriceFacade.getPrice(zone, instanceType);
      Srv = 1;
    } else {//spot instance
      Pmkt = getLinuxSpotPrice(instanceType, zone, experimentDate);
      Srv = estimateSpotReliability(instanceType, zone, bid, availabilityTime, experimentDate);
      //Instance average availability time, In case of failure
      Tfavg = estimateTerminationAverageRunTime(instanceType, zone, bid, availabilityTime, experimentDate);
    }

    // Instance availability time, round up in case of success for spot or Ondemand
    long Ts = (availabilityTime / ONE_HOUR_MILISECOND) + 1;

    double time = (Srv * Ts) + ((1 - Srv) * Tfavg);
    BigDecimal EPrv = Pmkt.multiply(new BigDecimal(time)).setScale(4, RoundingMode.HALF_UP);

    logger.debug("Estimated Profit: " + EPrv + " for Instance: " + instanceType + " / "
        + zone + " AvailabilityTime: " + availabilityTime + " Bid: " + bid);
    return EPrv;
  }

  /**
   * Filter out EC2 instances with minimum requirement of ECU, memory and storage. For both spot and On-demand type of
   * the instance goes through all available regions and availability zones, calculates the estimated profit regarding
   * the specified availabilityTime and reliabilityLowerBound, and returns the list sorted ascending of instances.
   *
   * This function uses thread pool. assign a thread to each instanceType and then collect results from all threads and
   * sort.
   *
   * @param availabilityTime
   * @param reliabilityLowerBound
   * @param minECU
   * @param minMemoryGB
   * @param minStorage
   * @param experimentDate
   * @return ascending sorted list of estimated profit of all filtered instances and their possible zones
   *
   * @throws se.kth.kandy.cloud.common.exception.ServiceRecommanderException
   * @throws java.lang.InterruptedException
   * @throws java.util.concurrent.ExecutionException
   */
  public List<Ec2Instance> findAllInstancesZonesEstimatedProfit(long availabilityTime, float reliabilityLowerBound,
      InstanceFilter.ECU minECU, float minMemoryGB, InstanceFilter.STORAGEGB minStorage, Date experimentDate)
      throws ServiceRecommanderException, InterruptedException, ExecutionException {

    List<String> filteredInstances = instanceFilter.filterEc2InstanceTypes(minECU, minMemoryGB, minStorage);
    List<Ec2Instance> instancesZonesProfitList = new ArrayList<>();

    ExecutorService threadPool = Executors.newFixedThreadPool(filteredInstances.size());
    Set<Future<List<Ec2Instance>>> set = new HashSet<>();

    for (String instanceType : filteredInstances) { //assign a thread to each instanceType/zones
      Callable instanceZonesCostThread = new InstanceZonesEstimatedProfitThread(availabilityTime, reliabilityLowerBound,
          instanceType, experimentDate, this);
      Future<List<Ec2Instance>> future = threadPool.submit(instanceZonesCostThread);
      set.add(future);
    }

    for (Future<List<Ec2Instance>> future : set) {
      //this is synchronous part, calling future.get() waits untill response is ready
      instancesZonesProfitList.addAll(future.get());
    }
    threadPool.shutdown();

    Collections.sort(instancesZonesProfitList); // sort the list ascending
    logger.debug("Total number of instanceType/zone: " + instancesZonesProfitList.size() + " Slb: "
        + reliabilityLowerBound + " AvailabilityTime: " + availabilityTime + " ExperimentDay: " + experimentDate);
    for (Ec2Instance instanceCost : instancesZonesProfitList) {
      logger.debug(instanceCost);
    }
    return instancesZonesProfitList;
  }

  /**
   * For both spot and On-demand type of the instance goes through all available regions and availability zones,
   * calculates the estimated profit regarding the specified availabilityTime and reliabilityLowerBound, and returns the
   * list sorted ascending of instance profit.
   *
   * @param availabilityTime
   * @param reliabilityLowerBound
   * @param instanceType
   * @param experimentDate
   * @return ascending sorted list of estimated profit of the instance and all possible zones
   * @throws ServiceRecommanderException
   */
  public List<Ec2Instance> findInstanceZonesEstimatedProfit(long availabilityTime, float reliabilityLowerBound,
      String instanceType, Date experimentDate) throws ServiceRecommanderException {

    List<Ec2Instance> instanceZonesEPList = estimateInstanceZonesProfit(availabilityTime,
        reliabilityLowerBound, instanceType, experimentDate);

    logger.debug("Filtered Ec2 instances list. Slb: " + reliabilityLowerBound
        + " AvailabilityTime: " + availabilityTime);
    for (Ec2Instance instanceCost : instanceZonesEPList) {
      logger.debug(instanceCost);
    }
    return instanceZonesEPList;
  }

  public List<Ec2Instance> estimateInstanceZonesProfit(long availabilityTime, float reliabilityLowerBound,
      String instanceType, Date experimentDate) throws ServiceRecommanderException {

    List<Ec2Instance> instanceZonesEPList = new ArrayList<>();

    List<String> regions = awsEc2InstancePriceFacade.getRegions(instanceType);
    for (String region : regions) { // ondemand
      BigDecimal estimatedProfit = estimateInstanceProfit(instanceType, region, BigDecimal.ZERO, availabilityTime,
          experimentDate).setScale(4, RoundingMode.HALF_UP);
      instanceZonesEPList.add(new Ec2Instance(instanceType, region, estimatedProfit,
          awsEc2InstancePriceFacade.getPrice(region, instanceType),
          Ec2Instance.INSTANCETYPE.ONDEMAND, BigDecimal.ZERO));
    }

    List<String> availabilityZones = awsEc2SpotInstanceFacade.getAvailabilityZones(instanceType);
    for (String availabilityZone : availabilityZones) { // spot
      BigDecimal bid = estimateMinBid(instanceType, availabilityZone, availabilityTime, reliabilityLowerBound,
          experimentDate);
      if (bid.compareTo(BigDecimal.ZERO) == 0) { // Instance/zone is deprecated
        continue;
      }
      BigDecimal estimatedProfit = estimateInstanceProfit(instanceType, availabilityZone, bid, availabilityTime,
          experimentDate);
      instanceZonesEPList.add(new Ec2Instance(instanceType, availabilityZone, estimatedProfit,
          getLinuxSpotPrice(instanceType, availabilityZone, experimentDate), Ec2Instance.INSTANCETYPE.SPOT, bid));
    }

    Collections.sort(instanceZonesEPList); // sort the list ascending
    return instanceZonesEPList;
  }

  /**
   * if experiment date is before 2 days ago, we should get the price from database. It is used for experiments that we
   * set the date back in time.
   *
   * @param instanceType
   * @param availabilityZone
   * @param experimentDate
   * @return
   */
  public BigDecimal getLinuxSpotPrice(String instanceType, String availabilityZone, final Date experimentDate) {

    BigDecimal price;
    Date now = new Date();
    if (experimentDate.getTime() < (now.getTime() - 172800000)) { //2 days ago
      price = awsEc2SpotInstanceFacade.getSpotPrice(instanceType, availabilityZone, experimentDate);
    } else {
      price = ec2ApiWrapper.getCurrentLinuxSpotPrice(instanceType, availabilityZone);
    }
    logger.debug(
        "Spot price " + instanceType + "/" + availabilityZone + " experimentTime: " + experimentDate + " : " + price);
    return price;
  }

}
