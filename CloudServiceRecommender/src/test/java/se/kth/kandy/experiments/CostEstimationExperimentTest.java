package se.kth.kandy.experiments;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.ejb.factory.EjbFactory;

/**
 * Integration Test
 *
 * @author hossein
 */
public class CostEstimationExperimentTest {

  private static final Logger logger = Logger.getLogger(CostEstimationExperimentTest.class);
  private CostEstimationExperiment costEstimationExperiment = null;

  @BeforeClass
  public void setUpClass() {
    costEstimationExperiment = EjbFactory.getInstance().getCostEstimationExperiment();
  }

  @Test
  public void testCalculateInstanceZoneSamplesCostError() throws Exception {
    costEstimationExperiment.calculateInstanceZoneSamplesCostError("r3.xlarge", "us-west-2c", 108000000L, 0.7f);
  }

  @Test
  public void testCalculateInstanceZonesCostError() throws Exception {
    costEstimationExperiment.calculateInstanceZonesCostError("r3.xlarge", 108000000L);
  }

  @Test
  public void testCostEstimationEvaluation() throws Exception {
    costEstimationExperiment.costEstimationEvaluation(3);
  }

}
