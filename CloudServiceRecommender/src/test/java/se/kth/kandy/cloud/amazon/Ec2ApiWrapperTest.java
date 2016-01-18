package se.kth.kandy.cloud.amazon;

import junit.framework.Assert;
import org.testng.annotations.Test;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;

/**
 *
 * @author hossein
 */
public class Ec2ApiWrapperTest {

  @Test
  public void testGetCurrentLinuxSpotPrice() throws ServiceRecommanderException {
    Assert.assertNotNull(Ec2ApiWrapper.getInstance().getCurrentLinuxSpotPrice("r3.xlarge", "us-west-2c"));
  }
}
