package se.kth.kandy.ejb.jpa;

import java.util.List;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.ejb.factory.EjbFactory;
import se.kth.kandy.model.AwsEc2SpotInstance;

/**
 * Integration test
 *
 * In order to perform testings, starts Open ejb, embedded container
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
  public void numInstancesTest() {
    Assert.assertNotNull(awsEc2SpotInstanceFacade.count());
  }

  @Test
  public void lastSamplingDateTest() {
    Assert.assertNotNull(awsEc2SpotInstanceFacade.getlastSamplingDate());
  }

  @Test
  public void spotListTest() {
    List<AwsEc2SpotInstance> instanceList = awsEc2SpotInstanceFacade.getSpotInstanceList("c3.4xlarge", "us-west-1a");
    Assert.assertNotNull(instanceList);
  }

}
