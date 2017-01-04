package se.kth.kandy.experiments;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.algorithm.Ec2Instance;
import se.kth.kandy.ejb.algorithm.InstanceFilter;
import se.kth.kandy.ejb.algorithm.MaxProfitInstanceEstimator;
import se.kth.kandy.ejb.jpa.AwsEc2InstancePriceFacade;
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;
import se.kth.kandy.ejb.notify.ServerPushFacade;
import se.kth.kandy.model.AwsEc2SpotInstance;

/**
 * Uses different set of spot history for learning and experimenting. Starts from {EXPERIMENT_TIMES} as anchor point, do
 * the estimations for 85 days prior to this point and test the estimations right after this day. Repeat this every
 * month for two more months from the anchor point.
 *
 * @author Hossein
 */
@Stateless
public class ProfitEstimationExperiment {

  @EJB
  MaxProfitInstanceEstimator maxProfitInstanceEstimator;
  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;
  @EJB
  private CostEstimationExperiment costEstimationExperiment;
  @EJB
  private InstanceFilter instanceFilter;
  @EJB
  private ServerPushFacade serverPushFacade;
  @EJB
  private AwsEc2InstancePriceFacade awsEc2InstancePriceFacade;
  @EJB
  private ChartGenerator chartGenerator;
  private static final Logger logger = Logger.getLogger(ProfitEstimationExperiment.class);

  public static final long ONE_HOUR_MILISECOND = 3600000L;
  public List<Float> reliabilityList = Arrays.asList(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f);
  //public List<Float> reliabilityList = Arrays.asList(0.4f);

  public static final Calendar[] EXPERIMENT_TIMES = {
    new GregorianCalendar(2016, Calendar.OCTOBER, 21, 7, 0, 0),
    new GregorianCalendar(2016, Calendar.NOVEMBER, 21, 7, 0, 0),
    new GregorianCalendar(2016, Calendar.DECEMBER, 21, 7, 0, 0)};

  ArrayList<InstanceZoneProfit>[] instanceZoneProfitsList = new ArrayList[EXPERIMENT_TIMES.length];

  public ProfitEstimationExperiment() {
    for (int dateIndex = 0; dateIndex < EXPERIMENT_TIMES.length; dateIndex++) {
      instanceZoneProfitsList[dateIndex] = new ArrayList<>();
    }
  }

  public void calculateIZSamplesTrueCostEstimatedProfitSpot(String instanceName,
      String availabilityZone, long availabilityTime, float reliabilityLowerBound)
      throws ServiceRecommanderException {

    List<AwsEc2SpotInstance> spotPricesList = awsEc2SpotInstanceFacade.getSpotInstanceList(instanceName,
        availabilityZone);

    for (int dateIndex = 0; dateIndex < EXPERIMENT_TIMES.length; dateIndex++) {

      long allocationTime = EXPERIMENT_TIMES[dateIndex].getTimeInMillis();
      BigDecimal bid = maxProfitInstanceEstimator.estimateMinBid(instanceName, availabilityZone, availabilityTime,
          reliabilityLowerBound, new Date(allocationTime));
      if (bid.compareTo(BigDecimal.ZERO) == 0) { //instance zone is deprecated
        return; // return empty list
      }
      double terminationAverageTime = maxProfitInstanceEstimator.estimateTerminationAverageRunTime(instanceName,
          availabilityZone, bid, availabilityTime, new Date(allocationTime));
      BigDecimal estimatedProfit = maxProfitInstanceEstimator.
          estimateInstanceProfit(instanceName, availabilityZone, bid, availabilityTime, new Date(allocationTime));

      InstanceZoneProfit instanceZoneProfit = new InstanceZoneProfit(costEstimationExperiment.
          calculateTrueEstimatedCostRelativeError(instanceName, availabilityZone, availabilityTime,
              reliabilityLowerBound, bid, allocationTime, terminationAverageTime, spotPricesList));
      instanceZoneProfit.setEstimatedProfit(estimatedProfit);
      instanceZoneProfit.setType(Ec2Instance.INSTANCETYPE.SPOT);
      logger.trace(instanceZoneProfit);
      instanceZoneProfitsList[dateIndex].add(instanceZoneProfit);
    }
  }

  public void calculateIZSamplesTrueCostEstimatedProfitOndemand(String instanceName,
      String region, long availabilityTime) throws ServiceRecommanderException {

    for (int dateIndex = 0; dateIndex < EXPERIMENT_TIMES.length; dateIndex++) {

      long allocationTime = EXPERIMENT_TIMES[dateIndex].getTimeInMillis();
      BigDecimal estimatedProfit = maxProfitInstanceEstimator.estimateInstanceProfit(instanceName, region,
          BigDecimal.ZERO, availabilityTime, new Date(allocationTime));

      InstanceZoneProfit instanceZoneProfit = new InstanceZoneProfit(new InstanceZoneCost(instanceName, region,
          availabilityTime, BigDecimal.ZERO, allocationTime, 1.0f, BigDecimal.ZERO));

      instanceZoneProfit.setSuccess(true);
      instanceZoneProfit.setEstimatedProfit(estimatedProfit);
      instanceZoneProfit.setEstimatedCost(estimatedProfit);
      instanceZoneProfit.setTrueCost(estimatedProfit);
      instanceZoneProfit.setType(Ec2Instance.INSTANCETYPE.ONDEMAND);
      logger.trace(instanceZoneProfit);
      instanceZoneProfitsList[dateIndex].add(instanceZoneProfit);
    }
  }

  public Boolean calculateIZonesTrueCostEstimatedProfit(String instanceType, long availabilityTime)
      throws ServiceRecommanderException, InterruptedException, ExecutionException {

    List<String> availabilityZones = awsEc2SpotInstanceFacade.getAvailabilityZones(instanceType);
    logger.debug("Start calculating trueCost/estimatedProfit SpotInstance: " + instanceType
        + "  Zones Num: " + availabilityZones.size());
    for (float slb : reliabilityList) {  //spot
      for (String availabilityZone : availabilityZones) {
        calculateIZSamplesTrueCostEstimatedProfitSpot(instanceType, availabilityZone, availabilityTime, slb);
      }
    }

    List<String> regions = awsEc2InstancePriceFacade.getRegions(instanceType);
    logger.debug("Start calculating trueCost/estimatedProfit OndemandInstance: " + instanceType
        + "  Regions Num: " + regions.size());
    for (String region : regions) { // ondemand
      calculateIZSamplesTrueCostEstimatedProfitOndemand(instanceType, region,
          availabilityTime);
    }
    logger.debug("Finished calculating trueCost/estimatedProfit Instance: " + instanceType);
    return true;
  }

  public void profitEstimationEvaluation(final int availabilityTimeHours, List<Float> slbList) throws Exception {
    if (slbList != null) {
      this.reliabilityList = slbList;
    }
    List<String> allInstances = instanceFilter.filterEc2InstanceTypes(InstanceFilter.ECU.ALL, 0.0f,
        InstanceFilter.STORAGEGB.ALL);
    //List<String> allInstances = Arrays.asList("r3.xlarge", "", "r3.4xlarge");

    for (int dateIndex = 0; dateIndex < EXPERIMENT_TIMES.length; dateIndex++) {
      instanceZoneProfitsList[dateIndex].clear();//it is global variable, should clean it
    }

    ExecutorService threadPool = Executors.newFixedThreadPool(allInstances.size());
    Set<Future<Boolean>> set = new HashSet<>();
    for (int k = 0; k < allInstances.size();) { //assign a thread to each instanceType (spot zones and ondemand regions)
      final String instanceType = allInstances.get(k);
      Callable thread = new Callable() {

        @Override
        public Boolean call() {
          try {
            return calculateIZonesTrueCostEstimatedProfit(instanceType, availabilityTimeHours * 3600 * 1000);
          } catch (ServiceRecommanderException | InterruptedException | ExecutionException ex) {
            logger.error(ex);
            return false;
          }
        }
      };

      Future<Boolean> future = threadPool.submit(thread);
      set.add(future);
      k += 2;  //experiment on half of the instance types
    }

    for (Future<Boolean> future : set) {
      if (Thread.currentThread().isInterrupted()) {
        throw new RuntimeException("Main thread is interuppted");
      }
      //this is synchronous part, calling future.get() waits untill response is ready
      boolean result = future.get();
      if (!result) {
        logger.error("Instance thread did not finish successfully");
      } else {
        logger.error("Instance thread finished successfully");
      }
    }
    threadPool.shutdown();
    logger.debug("All Instances Threads finished calcualtion.");

    List<InstanceZoneProfit> mergedProfitList = new ArrayList<>();
    for (int dateIndex = 0; dateIndex < EXPERIMENT_TIMES.length; dateIndex++) {
      mergedProfitList.addAll(instanceZoneProfitsList[dateIndex]);
    }
    for (InstanceZoneProfit instanceZoneProfit : mergedProfitList) {
      logger.debug(instanceZoneProfit);
    }

    costEstimationExperiment.calculatePREperSLB(mergedProfitList, availabilityTimeHours, reliabilityList);
    calculateTerminationRatesPerSLB(mergedProfitList, availabilityTimeHours);
    calculatePercentProfitErrorPerSLBandDate(availabilityTimeHours);
  }

  /**
   * Uses the inversion count of the true cost on the profit sorted list to estimate profit error against true cost
   *
   * @param availabilityTimeHours
   */
  public void calculatePercentProfitErrorPerSLBandDate(int availabilityTimeHours) {

    logger.debug("Start calculating percent profit estimation error");

    List<Integer[]> percentProfitErrorsPerDate = new ArrayList<>();
    for (int dateIndex = 0; dateIndex < EXPERIMENT_TIMES.length; dateIndex++) { // per experiment date

      //distribute experiments to SLB specified list
      ArrayList<InstanceZoneProfit>[] profitsPerSlb = new ArrayList[reliabilityList.size()];
      for (int slbIndex = 0; slbIndex < reliabilityList.size(); slbIndex++) {
        profitsPerSlb[slbIndex] = new ArrayList<>();
      }
      for (InstanceZoneProfit instanceZoneProfit : instanceZoneProfitsList[dateIndex]) {
        if (instanceZoneProfit.getType() == Ec2Instance.INSTANCETYPE.SPOT) {
          if (instanceZoneProfit.isSuccess()) { //terminated instances not counted
            int slbIndex = reliabilityList.indexOf(instanceZoneProfit.getReliability());
            profitsPerSlb[slbIndex].add(instanceZoneProfit);
          }
        } else { // ondemands should be added to all slb lists
          for (int slbIndex = 0; slbIndex < reliabilityList.size(); slbIndex++) {
            profitsPerSlb[slbIndex].add(instanceZoneProfit);
          }
        }
      }

      Integer percentProfitError[] = {0, 0, 0, 0, 0, 0};
      for (int slbIndex = 0; slbIndex < reliabilityList.size(); slbIndex++) {
        percentProfitError[slbIndex] = getPercentProfitErrorPerSlb(profitsPerSlb[slbIndex]);
      }
      logger.debug("Tr(hours): " + availabilityTimeHours + ", experiment date: " + EXPERIMENT_TIMES[dateIndex].
          getTime());
      for (int j = 0; j < reliabilityList.size(); j++) {
        logger.debug("Slb: " + reliabilityList.get(j) + ", percentProfitError: "
            + percentProfitError[j] + ", Total experiments with  different i,z(spot/ondemand): "
            + profitsPerSlb[j].size());
      }
      percentProfitErrorsPerDate.add(percentProfitError);
    }

    chartGenerator.jpgXYChartProfitError(percentProfitErrorsPerDate, reliabilityList, EXPERIMENT_TIMES);
  }

  /**
   * Sorts the instanceProfitList based on profit ascending. Then uses bubble sort to find the inversions of this list,
   * against true cost.
   *
   * Counts the number of swaps needed because true cost has decreased and did not follow profit. Finally makes a
   * percent out of it.
   *
   * @param izProfits - profits with the same SLB
   * @return
   */
  public int getPercentProfitErrorPerSlb(ArrayList<InstanceZoneProfit> izProfits) {
    Collections.sort(izProfits); // sort the list ascending based on profit, then check against true cost
    int n = izProfits.size();
    int numSwaps = 0;
    int numComparesTotal = 0;
    for (int i = 0; i < n; i++) { //bubble sort to find the inversions
      for (int j = 1; j < (n - i); j++) {

        numComparesTotal++;
        if (izProfits.get(j - 1).getTrueCost().compareTo(izProfits.get(j).getTrueCost()) == 1) {
          //swap the elements!
          BigDecimal temp = izProfits.get(j - 1).getTrueCost();
          izProfits.get(j - 1).setTrueCost(izProfits.get(j).getTrueCost());
          izProfits.get(j).setTrueCost(temp);
          numSwaps++;
        }
      }
    }
    int percentProfitError = (int) (((float) numSwaps / numComparesTotal) * 100);
    return percentProfitError;
  }

  public void calculateTerminationRatesPerSLB(List<InstanceZoneProfit> instanceZoneProfitList,
      int availabilityTimeHours) throws IOException {
    logger.debug("Start calculating Early termination rate");

    int numExperiments = 0;
    Integer numTerminations[] = {0, 0, 0, 0, 0, 0};
    Integer numInstanceTotal[] = {0, 0, 0, 0, 0, 0};
    Integer percentTerminations[] = {0, 0, 0, 0, 0, 0};
    for (InstanceZoneProfit instanceZoneProfit : instanceZoneProfitList) {
      if (instanceZoneProfit.getType() == Ec2Instance.INSTANCETYPE.SPOT) {
        numExperiments++;
        int index = reliabilityList.indexOf(instanceZoneProfit.getReliability());
        numInstanceTotal[index]++;
        if (!instanceZoneProfit.isSuccess()) {
          numTerminations[index]++;
        }
      }
    }
    for (int j = 0; j < reliabilityList.size(); j++) {
      percentTerminations[j] = (int) (((float) numTerminations[j] / numInstanceTotal[j]) * 100);
    }
    logger.debug("Total experiments with different i,z(spot),slb, date: " + numExperiments
        + ", Tr(hours): " + availabilityTimeHours);
    for (int j = 0; j < reliabilityList.size(); j++) {
      logger.debug("Slb: " + reliabilityList.get(j) + ", percentTerminationRate: " + percentTerminations[j]);
    }
    chartGenerator.jpgLineChartTerminationRate(percentTerminations, reliabilityList);
  }
}
