package se.kth.kandy.ejb.algorithm;

import static org.testng.Assert.assertNotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.ejb.algorithm.InstanceFilter.ECU;
import se.kth.kandy.ejb.algorithm.InstanceFilter.STORAGEGB;
import se.kth.kandy.ejb.factory.EjbFactory;

/**
 *
 * @author hossein
 */
public class InstanceFilterTest {

  private InstanceFilter instanceFilter = null;

  @BeforeClass
  public void setUpClass() {
    instanceFilter = EjbFactory.getInstance().getInstanceFilter();
  }

  @Test
  public void testFilterEc2InstanceTypes() {
    assertNotNull(instanceFilter.filterEc2InstanceTypes(ECU.ALL, 2f, STORAGEGB.ALL));
  }

  @Test
  public void testFilterEc2InstanceTypes1() {
    assertNotNull(instanceFilter.filterEc2InstanceTypes(InstanceFilter.ECU.FIXED116, 244f,
        InstanceFilter.STORAGEGB.HDD12000));
  }
}
