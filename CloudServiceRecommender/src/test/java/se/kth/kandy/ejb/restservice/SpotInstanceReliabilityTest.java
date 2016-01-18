package se.kth.kandy.ejb.restservice;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class SpotInstanceReliabilityTest {

  private static final Logger logger = Logger.getLogger(SpotInstanceReliabilityTest.class);
  private SpotInstanceReliability spotInstanceReliability = null;

  @BeforeClass
  public void setUpClass() {
    spotInstanceReliability = EjbFactory.getInstance().getSpotInstanceReliability();
  }

  @Test
  public void testEstimateSpotReliability() {
    float reliability = spotInstanceReliability.estimateSpotReliability("r3.xlarge", "us-west-2c",
        new BigDecimal(0.0350).setScale(4, RoundingMode.HALF_UP), 229727000L);
  }

  @Test
  public void testEstimateMinBid() throws ServiceRecommanderException {
    BigDecimal bid = spotInstanceReliability.estimateMinBid("d2.8xlarge", "us-east-1b", 229727000L, (float) 0.8);
  }

}
