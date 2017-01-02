package se.kth.kandy.ejb.algorithm;

import java.io.File;
import java.io.IOException;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
   * @param availabilityTime - just for creating the chart
   * @param experimentDate
   * @return sorted list of availability time of samples from past 85 days ascending.
   */
  public List<Long> getSpotSamplesAvailabilityTime(String instanceType, String availabilityZone, BigDecimal bid,
      long availabilityTime, Date experimentDate) { //availability time is just for creating the chart

    List<AwsEc2SpotInstance> spotPricesList = awsEc2SpotInstanceFacade.
        getSpotInstanceList(instanceType, availabilityZone);

    Calendar calStart = new GregorianCalendar();
    calStart.setTime(experimentDate);
    calStart.set(Calendar.HOUR_OF_DAY, 7);
    calStart.set(Calendar.MINUTE, 0);
    calStart.set(Calendar.SECOND, 0);
    calStart.set(Calendar.MILLISECOND, 0);

    calStart.setTimeInMillis(calStart.getTimeInMillis() - SpotInstanceItemReader.SAMPLING_PERIOD_LENGHT);
    calStart.set(Calendar.HOUR_OF_DAY, 7); //set the availabilityHours to 7:00 again, because of daylight saving time
    calStart.set(Calendar.MINUTE, 0);
    calStart.set(Calendar.SECOND, 0);
    calStart.set(Calendar.MILLISECOND, 0);

    // start from 85 days ago at 07:00:00
    long startSamplingTime = calStart.getTimeInMillis();
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
    jpgXYChartCreator(instancesAvailTimeList, availabilityTime);
    // sort availability times accending
    Collections.sort(instancesAvailTimeList);
    return instancesAvailTimeList;
  }

  /**
   * create x-y chart as JPEG file
   *
   * @param instancesAvailTimeList
   * @param expectedAvailabilityTime
   */
  protected void jpgXYChartCreator(List<Long> instancesAvailTimeList, long expectedAvailabilityTime) {

    final XYSeries availabilityTimes = new XYSeries("samples availability times");
    final XYSeries expecytedAvailabilityTime = new XYSeries("expected availability time");

    long maxAvailabilityTime = 0;
    for (int j = 0; j < instancesAvailTimeList.size(); j++) {

      float availabilityHours;
      if (instancesAvailTimeList.get(j) == Long.MAX_VALUE) {
        availabilityHours = maxAvailabilityTime / (float) 3600000;
      } else {
        availabilityHours = instancesAvailTimeList.get(j) / (float) 3600000;
      }
      availabilityTimes.add(j, availabilityHours);
      expecytedAvailabilityTime.add(j, expectedAvailabilityTime / (float) 3600000);

      if (instancesAvailTimeList.get(j) > maxAvailabilityTime) {
        maxAvailabilityTime = instancesAvailTimeList.get(j);
      }
    }

    final XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(availabilityTimes);
    dataset.addSeries(expecytedAvailabilityTime);

    JFreeChart lineChartObject = ChartFactory.createXYLineChart(
        "Availability time of the sample instances",
        "Instance launch day", "Availability time (hours)",
        dataset, PlotOrientation.VERTICAL,
        true, true, false);

    int width = 640; /* Width of the image */

    int height = 480; /* Height of the image */

    String fileName = "sampleInstancesAvailabilityTime.jpeg";
    File lineChart = new File(fileName);
    try {
      ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width, height);
    } catch (IOException ex) {
      logger.error("unable to save chart");
      return;
    }
    logger.trace(fileName + " generated");
  }

}
