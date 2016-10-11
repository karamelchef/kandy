package se.kth.kandy.ejb.restservice;

import java.io.IOException;
import org.apache.log4j.Logger;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.factory.EjbFactory;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.IoUtils;

/**
 * Integration test. Reads real data from database to calculate time and price for the specified cluster.
 *
 * In order to perform testings, starts embedded container.
 *
 * @author Hossein
 */
public class ClusterCostFacadeRESTTest {

  private static final Logger logger = Logger.getLogger(ClusterCostFacadeRESTTest.class);
  private ClusterCostFacadeREST clusterCostFacadeREST = null;

  @Test
  public void testEstimateAvailabilityTimeAndTrueCost() throws ServiceRecommanderException {
    try {
      //ec2 ondemand prices
      String yaml = IoUtils.readContentFromClasspath("se/kth/kandy/yaml/flink-on-hdfs-3node-aws-m3-med.yml");
      assertNotNull(clusterCostFacadeREST.estimateAvailabilityTimeAndTrueCost(yaml));
      // baremetal
      yaml = IoUtils.readContentFromClasspath("se/kth/kandy/yaml/StandaloneFlinkWithHDFSBareMetal.yml");
      assertNotNull(clusterCostFacadeREST.estimateAvailabilityTimeAndTrueCost(yaml));
      yaml = IoUtils.readContentFromClasspath("se/kth/kandy/yaml/test.yml");
      assertNotNull(clusterCostFacadeREST.estimateAvailabilityTimeAndTrueCost(yaml));

    } catch (KaramelException | IOException ex) {
      logger.error(ex.getMessage());
      fail("Could not caluclate time for cluster");
    }
  }

  @BeforeClass
  public void setUpClass() {
    clusterCostFacadeREST = EjbFactory.getInstance().getClusterCostFacadeREST();
  }
}
