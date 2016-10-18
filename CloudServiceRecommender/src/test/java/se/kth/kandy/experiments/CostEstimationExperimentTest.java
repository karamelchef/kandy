package se.kth.kandy.experiments;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
  public void testCalculateInstanceZoneCostError() throws Exception {
    costEstimationExperiment.calculateInstanceZoneCostError("r3.xlarge", "us-west-2c", new BigDecimal(0.0668).
        setScale(4, RoundingMode.HALF_UP), 108000000L);
  }

}
