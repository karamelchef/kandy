package se.kth.kandy.experiments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.log4j.Logger;
import se.kth.kandy.batch.SpotInstanceItemReader;
import se.kth.kandy.ejb.algorithm.MinCostInstanceEstimator;
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;
import se.kth.kandy.model.AwsEc2SpotInstance;

/**
 *
 * @author Hossein
 */
@Stateless
public class CostEstimationExperiment {

  @EJB
  MinCostInstanceEstimator minCostInstanceEstimator;
  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;
  private static final Logger logger = Logger.getLogger(CostEstimationExperiment.class);

  public static final long TEN_DAY_MILISECOND = 864000000L;
  public static final long ONE_HOUR_MILISECOND = 3600000L;

  public void evaluateCostEstimation() {

  }

  /**
   * generate a random availability time in millisecond
   *
   * @param maxTrHours
   * @return availability time in millisecond
   */
  protected long getRandomAvailabilityTime(int maxTrHours) {
    Random random = new Random();
    long availabilityTime = (random.nextInt(maxTrHours) + 1) * 360 * 1000; // convert to milisecond
    return availabilityTime;
  }

  /**
   *
   * @param instanceType
   * @param availabilityZone
   * @param bid
   * @param availabilityTime
   * @return
   */
  public List<InstanceZoneCost> calculateInstanceZoneCostError(String instanceType, String availabilityZone,
      BigDecimal bid, long availabilityTime) {

    List<AwsEc2SpotInstance> spotPricesList = awsEc2SpotInstanceFacade.
        getSpotInstanceList(instanceType, availabilityZone);

    double terminationAverageTime = minCostInstanceEstimator.estimateTerminationAverageRunTime(instanceType,
        availabilityZone, bid, availabilityTime);

    Calendar calStart = new GregorianCalendar();
    calStart.setTime(new Date());
    calStart.set(Calendar.HOUR_OF_DAY, 7);
    calStart.set(Calendar.MINUTE, 0);
    calStart.set(Calendar.SECOND, 0);
    calStart.set(Calendar.MILLISECOND, 0);
    // start from 85 days ago at 07:00:00
    long sampleInstanceAllocationTime = calStart.getTimeInMillis() - SpotInstanceItemReader.SAMPLING_PERIOD_LENGHT;
    List<InstanceZoneCost> instanceZoneCostList = new ArrayList<>();

    for (int sampleIndex = 0; sampleIndex < 8; sampleIndex++) { //8 samples every 10 day

      long processedInstanceHour = -1;
      long visitedInstanceHour = 0;
      long successInstanceHour = (long) (availabilityTime / ONE_HOUR_MILISECOND);
      BigDecimal hourlyPrice = BigDecimal.ZERO;
      for (AwsEc2SpotInstance spotInstance : spotPricesList) { // instanceList is sorted ascending
        long spotTime = spotInstance.getId().getTimeStamp().getTime();

        if (spotTime >= sampleInstanceAllocationTime) {

          if (instanceZoneCostList.size() == sampleIndex) {
            instanceZoneCostList.add(new InstanceZoneCost(instanceType, availabilityZone, availabilityTime,
                spotInstance.getPrice(), spotTime));
            hourlyPrice = spotInstance.getPrice();
          }

          long instanceUpTime = spotTime - instanceZoneCostList.get(sampleIndex).getStartTime();
          long currentInstanceHour = (long) (instanceUpTime / ONE_HOUR_MILISECOND);

          if (instanceUpTime >= availabilityTime) { // instance was successfully up for the desired availability time
            //calculate true cost in case of success
            if (currentInstanceHour > successInstanceHour) {
              instanceZoneCostList.get(sampleIndex).addTrueCost(hourlyPrice.multiply(new BigDecimal(
                  successInstanceHour - processedInstanceHour)));
            }
            if (currentInstanceHour == successInstanceHour) {
              instanceZoneCostList.get(sampleIndex).addTrueCost(hourlyPrice.multiply(new BigDecimal(
                  successInstanceHour - processedInstanceHour - 1)));
              instanceZoneCostList.get(sampleIndex).addTrueCost(spotInstance.getPrice());
            }
            instanceZoneCostList.get(sampleIndex).setSuccess(true);
            instanceZoneCostList.get(sampleIndex).calculateEstimatedCost(successInstanceHour + 1);
            instanceZoneCostList.get(sampleIndex).calculatePercentRelativeError();
            break;
          }

          if ((spotInstance.getPrice().compareTo(bid) == 1)) { // spot price exceeds bid, termination will happen
            //calculate true cost in case of failure
            instanceZoneCostList.get(sampleIndex).addTrueCost(hourlyPrice.multiply(new BigDecimal(
                currentInstanceHour - processedInstanceHour - 1))); //partial hour will be skipped
            instanceZoneCostList.get(sampleIndex).setSuccess(false);
            instanceZoneCostList.get(sampleIndex).calculateEstimatedCost(terminationAverageTime);
            instanceZoneCostList.get(sampleIndex).calculatePercentRelativeError();
            break;
          }

          if ((currentInstanceHour > processedInstanceHour) && (currentInstanceHour > visitedInstanceHour)) {
            /*to skip already visited instance hour an make sure this block is executed just onece when entering
             new instance hour*/
            visitedInstanceHour = currentInstanceHour;
            /* moved to next instance hour, distance may be more than one hour. should add the cost for successfully
             passed previous hours*/
            instanceZoneCostList.get(sampleIndex).addTrueCost(hourlyPrice.multiply(new BigDecimal(
                currentInstanceHour - processedInstanceHour - 1)));
            processedInstanceHour = currentInstanceHour - 1;
            hourlyPrice = spotInstance.getPrice();
          }
        }
      }
      sampleInstanceAllocationTime += TEN_DAY_MILISECOND;
    }
    for (InstanceZoneCost instanceZoneCost : instanceZoneCostList) {
      logger.debug(instanceZoneCost);
    }
    return instanceZoneCostList;
  }

}
