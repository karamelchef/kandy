package se.kth.kandy.ejb.algorithm;

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
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;
import se.kth.kandy.model.AwsEc2SpotInstance;

/**
 *
 * @author Hossein
 */
@Stateless
public class SpotInstanceStatistics {

  private static final Logger logger = Logger.getLogger(SpotInstanceStatistics.class);
  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;

  public static final long ONE_DAY_MILISECOND = 86400000L;

  /**
   * Generates a sorted list of spot samples availability time before they terminate regarding the specified bid.
   *
   * It uses Aws spot price history. It assumes there are instances allocating every day at 7:00 for the past 85 days
   * and calculate their availability time regarding the specified bid. Finally sort the availability list.
   *
   * @param instanceType
   * @param availabilityZone
   * @param bid
   * @return sorted list of availability time of samples from past 85 days ascending.
   */
  public List<Long> getSpotSamplesAvailabilityTime(String instanceType, String availabilityZone, BigDecimal bid) {

    List<AwsEc2SpotInstance> spotPricesList = awsEc2SpotInstanceFacade.
        getSpotInstanceList(instanceType, availabilityZone);

    Calendar calStart = new GregorianCalendar();
    calStart.setTime(new Date());
    calStart.set(Calendar.HOUR_OF_DAY, 7);
    calStart.set(Calendar.MINUTE, 0);
    calStart.set(Calendar.SECOND, 0);
    calStart.set(Calendar.MILLISECOND, 0);

    // start from 85 days ago at 07:00:00
    long startSamplingTime = calStart.getTimeInMillis() - SpotInstanceItemReader.SAMPLING_PERIOD_LENGHT;
    calStart.setTimeInMillis(startSamplingTime);
    logger.trace("First sampling date: " + calStart.getTime().toString());

    List<Long> instancesAvailTimeList = new ArrayList<>();

    int lastTerminationDateIndex = -1;
    int spotNumber = 0;

    // calculate availability time of samples
    for (AwsEc2SpotInstance spotInstance : spotPricesList) { // instanceList is sorted ascending
      long spotTime = spotInstance.getId().getTimeStamp().getTime();

      if (spotTime >= startSamplingTime) {
        spotNumber++;

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
    logger.trace("Number of sampling dates: " + instancesAvailTimeList.size());
    logger.trace("Total number of spot instance under experiment: " + spotNumber);
    calStart.setTimeInMillis(startSamplingTime + ((instancesAvailTimeList.size() - 1) * ONE_DAY_MILISECOND));
    logger.trace("Last sampling date: " + calStart.getTime().toString());

    logger.trace(instancesAvailTimeList);

    // sort availability times accending
    Collections.sort(instancesAvailTimeList);
    return instancesAvailTimeList;
  }
}
