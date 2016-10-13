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
public class InstanceZonesCostThread implements Callable<List<Ec2Instance>> {

  private final MinCostInstanceEstimator minCostInstanceEstimator;
  private static final Logger logger = Logger.getLogger(InstanceZonesCostThread.class);

  private final long availabilityTime;
  private final float reliabilityLowerBound;
  private final String instanceType;

  public InstanceZonesCostThread(long availabilityTime, float reliabilityLowerBound, String instanceType,
      MinCostInstanceEstimator minCostInstanceEstimator) {
    this.availabilityTime = availabilityTime;
    this.reliabilityLowerBound = reliabilityLowerBound;
    this.instanceType = instanceType;
    this.minCostInstanceEstimator = minCostInstanceEstimator;
  }

  @Override
  public List<Ec2Instance> call() throws Exception {
    try {
      logger.debug("Start calculating zones cost for the instace: " + instanceType);

      return minCostInstanceEstimator.estimateInstanceZonesCost(
          availabilityTime, reliabilityLowerBound, instanceType);
    } catch (ServiceRecommanderException ex) {
      logger.error("Fail to estimate zones cost for the instance: " + instanceType);
      return new ArrayList<>();
    }
  }

}
