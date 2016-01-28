package se.kth.kandy.ejb.restservice;

import static org.testng.Assert.assertNotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.ejb.factory.EjbFactory;
import se.kth.kandy.ejb.restservice.MaxProfitEstimator.ECU;
import se.kth.kandy.ejb.restservice.MaxProfitEstimator.STORAGEGB;

/**
 *
 * @author hossein
 */
public class MaxProfitEstimatorTest {

  private MaxProfitEstimator maxProfitEstimator = null;

  @BeforeClass
  public void setUpClass() {
    maxProfitEstimator = EjbFactory.getInstance().getMaxProfitEstimator();
  }

  @Test
  public void testFilterEc2InstanceTypes() {
    assertNotNull(maxProfitEstimator.filterEc2InstanceTypes(ECU.FIXED26, 2f, STORAGEGB.HDD12000));
  }

}
