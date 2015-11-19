package se.kth.kandy.ejb.jpa;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.ejb.factory.EjbFactory;

/**
 * Integration test
 *
 * In order to perform testings, starts embedded GlassFish
 *
 * @author Hossein
 */
public class AwsEc2SpotInstanceFacadeTest {

  private static final Logger logger = Logger.getLogger(AwsEc2SpotInstanceFacadeTest.class);
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade = null;

  @BeforeClass
  public void setup() {
    awsEc2SpotInstanceFacade = EjbFactory.getInstance().getAwsEc2SpotInstanceFacade();
  }

  @Test
  public void test() {
    Assert.assertNotNull(awsEc2SpotInstanceFacade.count());
  }

}
