package se.kth.kandy.experiments;

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
import se.kth.kandy.batch.SpotInstanceItemReader;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.algorithm.InstanceFilter;
import se.kth.kandy.ejb.algorithm.MaxProfitInstanceEstimator;
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;
import se.kth.kandy.ejb.notify.ServerPushFacade;
import se.kth.kandy.model.AwsEc2SpotInstance;

/**
 * Uses same set of spot history for learning and experimenting. it is 85 days prior to the day of the experiment.
 *
 * @author Hossein
 */
@Stateless
public class CostEstimationExperiment {

  @EJB
  MaxProfitInstanceEstimator maxProfitInstanceEstimator;
  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;
  @EJB
  private InstanceFilter instanceFilter;
  @EJB
  private ServerPushFacade serverPushFacade;
  @EJB
  private ChartGenerator chartGenerator;

  private static final Logger logger = Logger.getLogger(CostEstimationExperiment.class);

  public static final int NUMBER_OF_SAMPLES = 8;
  public static final long TEN_DAY_MILISECOND = 864000000L;
  public static final long ONE_HOUR_MILISECOND = 3600000L;
  public List<Float> reliabilityList = Arrays.asList(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f);

  /**
   * Calculates percent relative error for an instance type and zone. It means the difference between the true cost and
   * estimated cost. It is only for spot instances.
   *
   * simulates running of 8 instances of the specified InstanceType and Zone every 10 day in the window of past 85 days.
   * calculates true cost and estimated cost for each of them. and finally percent relative error.
   *
   * @param instanceType
   * @param availabilityZone
   * @param reliabilityLowerBound
   * @param availabilityTime
   * @return
   * @throws se.kth.kandy.cloud.common.exception.ServiceRecommanderException
   */
  public List<InstanceZoneCost> calculateInstanceZoneSamplesCostError(String instanceType, String availabilityZone,
      long availabilityTime, float reliabilityLowerBound) throws ServiceRecommanderException {
    Date experimentDate = new Date();
    List<InstanceZoneCost> instanceZoneCostList = new ArrayList<>();
    BigDecimal bid = maxProfitInstanceEstimator.estimateMinBid(instanceType, availabilityZone, availabilityTime,
        reliabilityLowerBound, experimentDate);

    if (bid.compareTo(BigDecimal.ZERO) == 0) { //instance zone is deprecated
      return instanceZoneCostList; // return empty list
    }

    List<AwsEc2SpotInstance> spotPricesList = awsEc2SpotInstanceFacade.
        getSpotInstanceList(instanceType, availabilityZone);

    double terminationAverageTime = maxProfitInstanceEstimator.estimateTerminationAverageRunTime(instanceType,
        availabilityZone, bid, availabilityTime, experimentDate);

    Calendar calStart = new GregorianCalendar();
    calStart.setTime(new Date());
    calStart.set(Calendar.HOUR_OF_DAY, 7);
    calStart.set(Calendar.MINUTE, 0);
    calStart.set(Calendar.SECOND, 0);
    calStart.set(Calendar.MILLISECOND, 0);
    // start from 85 days ago at 07:00:00
    long sampleInstanceAllocationTime = calStart.getTimeInMillis() - SpotInstanceItemReader.SAMPLING_PERIOD_LENGHT;

    for (int sampleIndex = 0; sampleIndex < NUMBER_OF_SAMPLES; sampleIndex++) { //8 samples every 10 day
      instanceZoneCostList.add(calculateTrueEstimatedCostRelativeError(instanceType, availabilityZone, availabilityTime,
          reliabilityLowerBound, bid, sampleInstanceAllocationTime, terminationAverageTime, spotPricesList));
      sampleInstanceAllocationTime += TEN_DAY_MILISECOND;
    }
    return instanceZoneCostList;
  }

  /**
   * Calculate true cost, estimated cost and relative error for v(i,z). it is for spot instances.
   *
   * experiment time for the instance should be specified.
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
   */
  public InstanceZoneCost calculateTrueEstimatedCostRelativeError(String i, String z, long tr, float slb,
      BigDecimal bid, long experimentTime, double tfavg, List<AwsEc2SpotInstance> spotPricesList) {

    long processedInstanceHour = -1;
    long visitedInstanceHour = 0;
    long successInstanceHour = (long) (tr / ONE_HOUR_MILISECOND);
    InstanceZoneCost instanceZoneCost = null;
    BigDecimal hourlyPrice = BigDecimal.ZERO;
    for (AwsEc2SpotInstance spotInstance : spotPricesList) { // instanceList is sorted ascending
      long spotTime = spotInstance.getId().getTimeStamp().getTime();

      if (spotTime >= experimentTime) {

        if (instanceZoneCost == null) {
          instanceZoneCost = new InstanceZoneCost(i, z, tr, spotInstance.getPrice(), spotTime, slb, bid);
          hourlyPrice = spotInstance.getPrice();
        }

        long instanceUpTime = spotTime - instanceZoneCost.getStartTime();
        long currentInstanceHour = (long) (instanceUpTime / ONE_HOUR_MILISECOND);

        if (instanceUpTime >= tr) { // instance was successfully up for the desired availability time
          //calculate true cost in case of success
          if (currentInstanceHour > successInstanceHour) {
            instanceZoneCost.addTrueCost(hourlyPrice.multiply(new BigDecimal(
                successInstanceHour - processedInstanceHour)));
          }
          if (currentInstanceHour == successInstanceHour) {
            instanceZoneCost.addTrueCost(hourlyPrice.multiply(new BigDecimal(
                successInstanceHour - processedInstanceHour - 1)));
            instanceZoneCost.addTrueCost(spotInstance.getPrice());
          }
          instanceZoneCost.setSuccess(true);
          instanceZoneCost.calculateEstimatedCost(successInstanceHour + 1);
          instanceZoneCost.calculatePercentRelativeError();
          break;
        }

        if ((spotInstance.getPrice().compareTo(bid) == 1)) { // spot price exceeds bid, termination will happen
          //calculate true cost in case of failure
          instanceZoneCost.addTrueCost(hourlyPrice.multiply(new BigDecimal(
              currentInstanceHour - processedInstanceHour - 1))); //partial hour will be skipped
          instanceZoneCost.setSuccess(false);
          instanceZoneCost.calculateEstimatedCost(tfavg);
          instanceZoneCost.calculatePercentRelativeError();
          break;
        }

        if ((currentInstanceHour > processedInstanceHour) && (currentInstanceHour > visitedInstanceHour)) {
          /*to skip already visited instance hour an make sure this block is executed just onece when entering
           new instance hour*/
          visitedInstanceHour = currentInstanceHour;
          /* moved to next instance hour, distance may be more than one hour. should add the cost for successfully
           passed previous hours*/
          instanceZoneCost.addTrueCost(hourlyPrice.multiply(new BigDecimal(
              currentInstanceHour - processedInstanceHour - 1)));
          processedInstanceHour = currentInstanceHour - 1;
          hourlyPrice = spotInstance.getPrice();
        }
      }
    }
    return instanceZoneCost;
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

    logger.debug("Start calculating relativeError instaceType: " + instanceType
        + "  availabilityZones Num: " + availabilityZones.size());
    for (float slb : reliabilityList) {
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
   * different Slb. If slb list not provided, will take default one
   *
   * @param availabilityTimeHours
   * @param slbList
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public void costEstimationEvaluation(final int availabilityTimeHours, List<Float> slbList) throws Exception {

    if (slbList != null) {
      this.reliabilityList = slbList;
    }
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
    calculatePREperSLB(instanceZoneCostList, availabilityTimeHours, reliabilityList);
  }

  /**
   * Just for spot instances
   *
   * @param instanceZoneCostList
   * @param availabilityTimeHours
   * @param reliabilityList
   * @throws IOException
   */
  public void calculatePREperSLB(List<?> instanceZoneCostList, int availabilityTimeHours,
      List<Float> reliabilityList) throws IOException {
    logger.debug("Start calculating Cost estimation percent relative error");

    int numExperiments = 0;
    List<BigDecimal> relativeErrorDistributationList = new ArrayList<>();
    BigDecimal percentRelativeErrorList[] = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
      BigDecimal.ZERO, BigDecimal.ZERO};
    Integer experimentNumberList[] = {0, 0, 0, 0, 0, 0};
    for (Object object : instanceZoneCostList) {
      InstanceZoneCost instanceZoneCost = (InstanceZoneCost) object;
      if (instanceZoneCost.getTrueCost().compareTo(BigDecimal.ZERO) == 1 // true cost bigger than 0
          && instanceZoneCost.getPercentRelativeError().compareTo(new BigDecimal(101)) == -1
          // relative error less than 101
          && instanceZoneCost.getMarketHourlyPrice().compareTo(BigDecimal.ZERO) == 1) {
        // Pmkt is not set for Ondemand instances
        numExperiments++;
        relativeErrorDistributationList.add(instanceZoneCost.getPercentRelativeError());
        //add all errors for each slb and count number of them
        int slbIndex = reliabilityList.indexOf(instanceZoneCost.getReliability());
        percentRelativeErrorList[slbIndex]
            = instanceZoneCost.getPercentRelativeError().add(percentRelativeErrorList[slbIndex]);
        experimentNumberList[slbIndex]++;
      }
    }
    for (int j = 0; j < reliabilityList.size(); j++) {

      if (experimentNumberList[j] == 0) { // there was no good pre in this slb
        percentRelativeErrorList[j] = new BigDecimal(100);
      } else {
        //calculate average percent relative error for each Slb
        percentRelativeErrorList[j] = percentRelativeErrorList[j].
            divide(new BigDecimal(experimentNumberList[j]), 2);
      }
    }

    logger.debug("Total experiments with different i,z(spot),slb, date: " + numExperiments
        + ", Tr(hours): " + availabilityTimeHours);
    for (int j = 0; j < reliabilityList.size(); j++) {
      logger.debug("Slb: " + reliabilityList.get(j) + ", RelativeError: " + percentRelativeErrorList[j]);
    }
    chartGenerator.jpgLineChartCostPRE(percentRelativeErrorList, reliabilityList);
    chartGenerator.jpgScatterPlotCostPREdistribution(relativeErrorDistributationList);
  }
}
