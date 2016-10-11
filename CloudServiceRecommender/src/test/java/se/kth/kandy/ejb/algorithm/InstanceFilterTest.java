package se.kth.kandy.ejb.algorithm;

import se.kth.kandy.ejb.algorithm.InstanceFilter;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.ejb.factory.EjbFactory;
import se.kth.kandy.ejb.algorithm.InstanceFilter.ECU;
import se.kth.kandy.ejb.algorithm.InstanceFilter.STORAGEGB;

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
    assertNotNull(instanceFilter.filterEc2InstanceTypes(ECU.FIXED26, 2f, STORAGEGB.HDD12000));
  }

}
