package se.kth.kandy.ejb.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;

/**
 *
 * @author Hossein
 */
public class InstanceZonesEstimatedProfitThread implements Callable<List<Ec2Instance>> {

  private final MaxProfitInstanceEstimator maxProfitInstanceEstimator;
  private static final Logger logger = Logger.getLogger(InstanceZonesEstimatedProfitThread.class);

  private final long availabilityTime;
  private final float reliabilityLowerBound;
  private final String instanceType;

  public InstanceZonesEstimatedProfitThread(long availabilityTime, float reliabilityLowerBound, String instanceType,
      MaxProfitInstanceEstimator maxProfitInstanceEstimator) {
    this.availabilityTime = availabilityTime;
    this.reliabilityLowerBound = reliabilityLowerBound;
    this.instanceType = instanceType;
    this.maxProfitInstanceEstimator = maxProfitInstanceEstimator;
  }

  @Override
  public List<Ec2Instance> call() throws Exception {
    try {
      logger.debug("Start calculating zones estimated profit for the instace: " + instanceType);

      return maxProfitInstanceEstimator.estimateInstanceZonesProfit(
          availabilityTime, reliabilityLowerBound, instanceType);
    } catch (ServiceRecommanderException ex) {
      logger.error("Failed to estimate zones profit for the instance: " + instanceType);
      return new ArrayList<>();
    }
  }

}
