package se.kth.kandy.experiments;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import se.kth.kandy.batch.SpotInstanceItemReader;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.algorithm.InstanceFilter;
import se.kth.kandy.ejb.algorithm.MaxProfitInstanceEstimator;
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;
import se.kth.kandy.ejb.notify.ServerPushFacade;
import se.kth.kandy.model.AwsEc2SpotInstance;

/**
 *
 * @author Hossein
 */
@Stateless
public class CostEstimationExperiment {

  @EJB
  MaxProfitInstanceEstimator minCostInstanceEstimator;
  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;
  @EJB
  private InstanceFilter instanceFilter;
  @EJB
  private ServerPushFacade serverPushFacade;

  private static final Logger logger = Logger.getLogger(CostEstimationExperiment.class);

  public static final long TEN_DAY_MILISECOND = 864000000L;
  public static final long ONE_HOUR_MILISECOND = 3600000L;
  public static final List<Integer> AVAILABILITY_TIME_HOURS = Arrays.asList(3, 12, 24, 60, 100);
  public static final List<Float> RELIABILITY_LOWER_BOUNDS = Arrays.asList(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f);

  /**
   * Calculates percent relative error for an instance type and zone. It means the difference between the true cost and
   * estimated cost. It is only for spot instances.
   *
   * simulates running of 8 instances of the specified InstanceType and Zone every 10 day in the window of past 85 days.
   * calculates true cost and estimated cost for each of them. and finally percent relative error.
   *
   * TrueCost [Success] = Total of (beginning of each instance hour market price) //round up last partial hour
   *
   * TrueCost [Failure] = Total of (beginning of each instance hour market price) //omit last partial hour
   *
   * EstimatedCost [Success] = Pmkt (at launch time) * ceiling (availabilityTime)
   *
   * EstimatedCost [Failure] = Pmkt (at launch time) * termination average runtime
   *
   * percent relative Error = (|trueCost - estimatedCost| / trueCost) * 100
   *
   * @param instanceType
   * @param availabilityZone
   * @param bid
   * @param reliabilityLowerBound
   * @param availabilityTime
   * @return
   * @throws se.kth.kandy.cloud.common.exception.ServiceRecommanderException
   */
  public List<InstanceZoneCost> calculateInstanceZoneSamplesCostError(String instanceType, String availabilityZone,
      long availabilityTime, float reliabilityLowerBound) throws ServiceRecommanderException {

    List<InstanceZoneCost> instanceZoneCostList = new ArrayList<>();
    BigDecimal bid = minCostInstanceEstimator.estimateMinBid(instanceType, availabilityZone, availabilityTime,
        reliabilityLowerBound);

    if (bid.compareTo(BigDecimal.ZERO) == 0) { //instance zone is deprecated
      return instanceZoneCostList; // return empty list
    }

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
                spotInstance.getPrice(), spotTime, reliabilityLowerBound, bid));
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
    return instanceZoneCostList;
  }

  /**
   * Calculates percent relative error for all availabilityZones of an instance type with different reliability. Only
   * experiments spot instances.
   *
   * @param instanceType
   * @param availabilityTime
   * @return
   * @throws ServiceRecommanderException
   * @throws java.lang.InterruptedException
   * @throws java.util.concurrent.ExecutionException
   */
  public List<InstanceZoneCost> calculateInstanceZonesCostError(String instanceType, long availabilityTime)
      throws ServiceRecommanderException, InterruptedException, ExecutionException {

    List<InstanceZoneCost> instanceZoneCostList = new ArrayList<>();
    List<String> availabilityZones = awsEc2SpotInstanceFacade.getAvailabilityZones(instanceType);

    for (float slb : RELIABILITY_LOWER_BOUNDS) {
      for (String availabilityZone : availabilityZones) { // spot
        instanceZoneCostList.addAll(calculateInstanceZoneSamplesCostError(instanceType, availabilityZone,
            availabilityTime, slb));
      }
    }

    for (InstanceZoneCost instanceZoneCost : instanceZoneCostList) {
      logger.debug(instanceZoneCost);
    }
    return instanceZoneCostList;
  }

  /**
   * In fixed availabilityTime, calculates percent relative error for all possible spot (instance,availabilityZone) and
   * different Slb
   *
   * @param availabilityTimeHours
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public void costEstimationEvaluation(final int availabilityTimeHours) throws Exception {

    List<String> allInstances = instanceFilter.filterEc2InstanceTypes(InstanceFilter.ECU.ALL, 0.0f,
        InstanceFilter.STORAGEGB.ALL);
    //List<String> allInstances = Arrays.asList("r3.xlarge");

    List<InstanceZoneCost> instanceZoneCostList = new ArrayList<>();

    ExecutorService threadPool = Executors.newFixedThreadPool(allInstances.size());
    Set<Future<List<InstanceZoneCost>>> set = new HashSet<>();

    for (int k = 0; k < allInstances.size();) { //assign a thread to each instanceType/zones
      final String instanceType = allInstances.get(k);
      Callable thread = new Callable() {

        @Override
        public List<InstanceZoneCost> call() throws Exception {
          try {
            logger.debug("Start calculating zones cost for the instace: " + instanceType);
            return calculateInstanceZonesCostError(instanceType, availabilityTimeHours * 3600 * 1000);
          } catch (ServiceRecommanderException ex) {
            logger.error(ex);
            return new ArrayList<>();
          }
        }
      };
      Future<List<InstanceZoneCost>> future = threadPool.submit(thread);
      set.add(future);
      k += 2;  //experiment on half of the instance types
    }

    for (Future<List<InstanceZoneCost>> future : set) {
      //this is synchronous part, calling future.get() waits untill response is ready
      instanceZoneCostList.addAll(future.get());
    }
    threadPool.shutdown();

    logger.debug("Start calculating percent relative error");

    BigDecimal percentRelativeErrorList[] = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
      BigDecimal.ZERO, BigDecimal.ZERO};
    Integer experimentNumberList[] = {0, 0, 0, 0, 0, 0};
    for (InstanceZoneCost instanceZoneCost : instanceZoneCostList) {
      if (instanceZoneCost.getTrueCost().compareTo(BigDecimal.ZERO) == 1 // true cost bigger than 0
          && instanceZoneCost.getPercentRelativeError().compareTo(new BigDecimal(101)) == -1) {
        // relative error less than 101

        //add all errors for each slb and count number of them
        int index = RELIABILITY_LOWER_BOUNDS.indexOf(instanceZoneCost.getReliability());
        percentRelativeErrorList[index]
            = instanceZoneCost.getPercentRelativeError().add(percentRelativeErrorList[index]);
        experimentNumberList[index]++;
      }
    }
    for (int j = 0; j < RELIABILITY_LOWER_BOUNDS.size(); j++) {
      //calculate average percent relative error for each Slb
      percentRelativeErrorList[j] = percentRelativeErrorList[j].
          divide(new BigDecimal(experimentNumberList[j]), 2);
    }

    logger.debug("Total experiments: " + instanceZoneCostList.size() + ", Tr(hours): " + availabilityTimeHours);
    for (int j = 0; j < RELIABILITY_LOWER_BOUNDS.size(); j++) {
      logger.debug("Slb: " + RELIABILITY_LOWER_BOUNDS.get(j) + ", RelativeError: " + percentRelativeErrorList[j]);
    }
    jpgLineChartCreator(percentRelativeErrorList, RELIABILITY_LOWER_BOUNDS);
  }

  /**
   * create line chart as JPEG file
   *
   * on Linux jpeg file is created under /payara41/glassfish/domains/domain1/config
   *
   * @param relativeErrors
   * @param reliabilities
   * @throws IOException
   */
  protected void jpgLineChartCreator(BigDecimal[] relativeErrors, List<Float> reliabilities) throws IOException {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for (int j = 0; j < reliabilities.size(); j++) {
      dataset.addValue(relativeErrors[j], "", reliabilities.get(j));
    }
    JFreeChart lineChartObject = ChartFactory.createLineChart(
        "Percent relative error of CostEstimation",
        "Reliability Lower Bound (Slb)", "Percent relative Error",
        dataset, PlotOrientation.VERTICAL,
        true, true, false);

    int width = 640; /* Width of the image */

    int height = 480; /* Height of the image */

    String fileName = "costRelativeError.jpeg";
    File lineChart = new File(fileName);
    ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width, height);
    logger.debug(fileName + " created");
  }
}
