package se.kth.kandy.experiments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.ejb.factory.EjbFactory;

/**
 *
 * @author Hossein
 */
public class ProfitEstimationExperimentTest {

  private static final Logger logger = Logger.getLogger(ProfitEstimationExperimentTest.class);
  private ProfitEstimationExperiment profitEstimationExperiment = null;

  @BeforeClass
  public void setUpClass() {
    profitEstimationExperiment = EjbFactory.getInstance().getProfitEstimationExperiment();
  }

  @Test
  public void testCalculateInstanceZoneSamplesTrueCost() throws Exception {
    profitEstimationExperiment.calculateIZSpotSamplesTrueCostEstimatedCostEstimatedProfit(
        "r3.xlarge", "us-west-2c", 43200000L, 0.7f);
    profitEstimationExperiment.calculateIZOndemandSamplesTrueCostEstimatedProfit(
        "r3.xlarge", "us-west-2", 43200000L);
  }

  @Test
  public void testCalculateInstanceZonesTrueCost() throws Exception {
    profitEstimationExperiment.calculateIZonesTrueCostEstimatedProfit("r3.xlarge", 43200000L);
  }

  @Test
  public void testProfitEstimationEvaluation() throws Exception {
    profitEstimationExperiment.costProfitEstimationEvaluation(3, null);
  }

  @Test
  public void testProfitEstimationEvaluation1() throws Exception {
    profitEstimationExperiment.costProfitEstimationEvaluation(3, Arrays.asList(0.8f));
  }

  //------------------------------------------------------------------------------------------
  @Test
  public void testPercentProfitErrorPerSlb() {
    ArrayList<InstanceZoneProfit> izProfits = new ArrayList<>();

    InstanceZoneCost cost = new InstanceZoneCost();
    cost.setTrueCost(new BigDecimal(14));
    InstanceZoneProfit profit = new InstanceZoneProfit(cost);
    profit.setEstimatedProfit(new BigDecimal(0));
    izProfits.add(profit);

    cost.setTrueCost(new BigDecimal(10));
    profit = new InstanceZoneProfit(cost);
    profit.setEstimatedProfit(new BigDecimal(3));
    izProfits.add(profit);

    cost.setTrueCost(new BigDecimal(33));
    profit = new InstanceZoneProfit(cost);
    profit.setEstimatedProfit(new BigDecimal(1));
    izProfits.add(profit);

    cost.setTrueCost(new BigDecimal(27));
    profit = new InstanceZoneProfit(cost);
    profit.setEstimatedProfit(new BigDecimal(2));
    izProfits.add(profit);

    Assert.assertEquals(profitEstimationExperiment.getPercentProfitErrorPerSlb(izProfits), 66);
  }
}
