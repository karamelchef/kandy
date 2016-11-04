package se.kth.kandy.ejb.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.factory.EjbFactory;

/**
 * Integration test
 *
 * @author hossein
 */
public class MinCostInstanceEstimatorTest {

  private static final Logger logger = Logger.getLogger(MinCostInstanceEstimatorTest.class);
  private MinCostInstanceEstimator minCostInstanceEstimator = null;

  @BeforeClass
  public void setUpClass() {
    minCostInstanceEstimator = EjbFactory.getInstance().getMinCostInstanceEstimator();
  }

  //------------------------------------------------------------------
  @Test
  public void testEstimateSpotReliability() {
    float reliability = minCostInstanceEstimator.estimateSpotReliability("r3.xlarge", "us-west-2c",
        new BigDecimal(0.15).setScale(4, RoundingMode.HALF_UP), 108000000L);
  }

  @Test
  public void testEstimateSpotReliability1() {
    float reliability = minCostInstanceEstimator.estimateSpotReliability("d2.2xlarge", "us-east-1d",
        new BigDecimal(0.08).setScale(4, RoundingMode.HALF_UP), 229727000L);
  }

  //-------------------------------------------------------------------
  @Test
  public void testEstimateTerminationAverageRunTime() {
    double avgTime = minCostInstanceEstimator.estimateTerminationAverageRunTime("r3.xlarge", "us-west-2c",
        new BigDecimal(0.15).setScale(4, RoundingMode.HALF_UP), 108000000L);
  }

  //-------------------------------------------------------------------
  @Test
  public void testEstimateMinBid3() throws ServiceRecommanderException {
    BigDecimal bid = minCostInstanceEstimator.estimateMinBid("d2.8xlarge", "eu-central-1b", 229727000L, (float) 0.7);
  }

  @Test
  public void testEstimateMinBid2() throws ServiceRecommanderException {
    BigDecimal bid = minCostInstanceEstimator.estimateMinBid("d2.2xlarge", "us-east-1d", 229727000L, (float) 0.7);
  }

  @Test
  public void testEstimateMinBid1() throws ServiceRecommanderException {
    BigDecimal bid = minCostInstanceEstimator.estimateMinBid("r3.xlarge", "us-west-2c", 108000000L, (float) 0.7);
  }

  @Test
  public void testEstimateMinBid() throws ServiceRecommanderException {
    BigDecimal bid = minCostInstanceEstimator.estimateMinBid("g2.2xlarge", "us-west-1a", 43200000L, (float) 0.8);
  }

  //---------------------------------------------------------------------
  @Test
  public void testEstimateInstanceCost() throws ServiceRecommanderException {
    BigDecimal cost = minCostInstanceEstimator.estimateInstanceCost("r3.xlarge", "us-west-2c",
        new BigDecimal(0.15).setScale(4, RoundingMode.HALF_UP), 108000000L);
  }

  @Test
  public void testEstimateInstanceCost1() throws ServiceRecommanderException {
    BigDecimal cost = minCostInstanceEstimator.estimateInstanceCost("d2.2xlarge", "eu-central-1b",
        new BigDecimal(0.3).setScale(4, RoundingMode.HALF_UP), 229727000L);
  }

  //------------------------------------------------------------------------
  @Test
  void testFindAllInstancesZonesCost() throws Exception {
    List<Ec2Instance> instances = minCostInstanceEstimator.findAllInstancesZonesCost(229727000L, (float) 0.7,
        InstanceFilter.ECU.FIXED26, 2f, InstanceFilter.STORAGEGB.HDD12000);
  }

  @Test
  void testFindAllInstancesZonesCost1() throws Exception {
    List<Ec2Instance> instances = minCostInstanceEstimator.findAllInstancesZonesCost(229727000L, (float) 0.7,
        InstanceFilter.ECU.FIXED116, 244f, InstanceFilter.STORAGEGB.HDD12000);
  }

  @Test
  void testFindAllInstancesZonesCost2() throws Exception {
    List<Ec2Instance> instances = minCostInstanceEstimator.findAllInstancesZonesCost(229727000L, (float) 0.7,
        InstanceFilter.ECU.ALL, 0f, InstanceFilter.STORAGEGB.ALL);
  }

  //---------------------------------------------------------------------------
  @Test
  void testFindInstanceZonesCost() throws ServiceRecommanderException {
    List<Ec2Instance> instances = minCostInstanceEstimator.findInstanceZonesCost(229727000L, (float) 0.7, "c4.8xlarge");
  }

  //---------------------------------------------------------------------------
}
