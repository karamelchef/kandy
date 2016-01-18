package se.kth.kandy.ejb.restservice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.log4j.Logger;
import se.kth.kandy.batch.SpotInstanceItemReader;
import se.kth.kandy.cloud.amazon.Ec2ApiWrapper;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;
import se.kth.kandy.model.AwsEc2SpotInstance;

/**
 * Based on empirical data (Spot history prices) estimates the reliability of an instance for the user requested time.
 * Also bid is estimated to meet market price and reliability lower bound
 *
 * @author Hossein
 */
@Stateless
public class SpotInstanceReliability {

  private static final Logger logger = Logger.getLogger(SpotInstanceReliability.class);
  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;

  public static final long ONE_DAY_MILISECOND = 86400000L;

  /**
   * Estimate the probability that specified instance will be available for the user request time regarding the
   * specified bid and availability zone.
   *
   * It uses Aws spot price history and kaplan-meier estimator. It assumes there are instances allocating every day at
   * 7:00 for the past 60 days and calculate their availability time regarding the specified bid. Finally sort the
   * availability list and uses Kaplan-Meier estimator to calculate empirical probability of success.
   *
   * @param instanceType
   * @param availabilityZone
   * @param bid
   * @param availabilityTime in millisecond
   * @return empirical probability of success between [0,1] - S(r,v)
   */
  public float estimateSpotReliability(String instanceType, String availabilityZone, BigDecimal bid,
      long availabilityTime) {

    List<AwsEc2SpotInstance> spotPricesList = awsEc2SpotInstanceFacade.
        getSpotInstanceList(instanceType, availabilityZone);
    if (spotPricesList.isEmpty()) {
      return 0;
    }

    Calendar calStart = new GregorianCalendar();
    calStart.setTime(new Date());
    calStart.set(Calendar.HOUR_OF_DAY, 7);
    calStart.set(Calendar.MINUTE, 0);
    calStart.set(Calendar.SECOND, 0);
    calStart.set(Calendar.MILLISECOND, 0);
    // start from 60 days ago at 07:00:00
    long startSamplingTime = calStart.getTimeInMillis() - SpotInstanceItemReader.SAMPLING_PERIOD_LENGHT;

    List<Long> instancesAvailTimeList = new ArrayList<>();

    int lastTerminationDateIndex = -1;

    // calculate availability time of samples
    for (AwsEc2SpotInstance spotInstance : spotPricesList) { // instanceList is sorted ascending
      long spotTime = spotInstance.getId().getTimeStamp().getTime();
      if (spotTime >= startSamplingTime) {

        int currentSamplingDateIndex = (int) ((spotTime - startSamplingTime) / ONE_DAY_MILISECOND);
        if ((spotInstance.getPrice().compareTo(bid) == 1)) { // spot price exceeds bid, termination will happen
          if (currentSamplingDateIndex == lastTerminationDateIndex) {
            continue; // if termination happen for a day, next fetched spot prices will be skipped untill starting the
            // next day
          }
          long mod = ((spotTime - startSamplingTime) % ONE_DAY_MILISECOND);
          for (int i = lastTerminationDateIndex + 1; i <= currentSamplingDateIndex; i++) {
            long availableTime = ((currentSamplingDateIndex - i) * ONE_DAY_MILISECOND) + mod;
            instancesAvailTimeList.add(availableTime);
          }
          lastTerminationDateIndex = currentSamplingDateIndex;
        } else if (spotPricesList.get(spotPricesList.size() - 1).getId().getTimeStamp().getTime() == spotTime) {
          // if last fetched spot instance from the list has been processed and was not terminated
          while (currentSamplingDateIndex >= instancesAvailTimeList.size()) {
            // last samples that where not terminated, we need to update their availability time with max
            instancesAvailTimeList.add(Long.MAX_VALUE);
          }
        }
      }
    }

    // sort availability times accending
    Collections.sort(instancesAvailTimeList);
    float probabilityOfSuccess = 1; // if there would be no termination
    // calcualte empirical probability of success with Kaplan-Meier estimator
    for (int i = 0; (i < instancesAvailTimeList.size() - 1)
        && (instancesAvailTimeList.get(i) <= availabilityTime); i++) {
      // if there would be termination
      int NxV = instancesAvailTimeList.size() - i - 1;
      probabilityOfSuccess *= ((float) (NxV - 1)) / NxV;
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
   * @return bid $/h
   * @throws ServiceRecommanderException
   */
  public BigDecimal estimateMinBid(String instanceType, String availabilityZone, long availabilityTime,
      float reliabilityLowerBound) throws ServiceRecommanderException {
    // starts the bid from current spot market price
    BigDecimal bid = Ec2ApiWrapper.getInstance().getCurrentLinuxSpotPrice(instanceType, availabilityZone);
    while (estimateSpotReliability(instanceType, availabilityZone, bid, availabilityTime) < reliabilityLowerBound) {
      bid = bid.add(new BigDecimal("0.003")); // increase the bid in $
    }
    return bid;
  }
}
