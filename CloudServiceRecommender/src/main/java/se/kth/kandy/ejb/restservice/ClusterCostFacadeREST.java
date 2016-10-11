package se.kth.kandy.ejb.restservice;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.algorithm.ClusterCost;
import se.kth.kandy.ejb.algorithm.Ec2Instance;
import se.kth.kandy.ejb.algorithm.MinCostInstanceEstimator;
import se.kth.kandy.json.cost.ClusterTimePrice;
import se.kth.karamel.common.exception.KaramelException;

/**
 * JaxRS rest service implemented as EJB.
 *
 * Calculates expected cost(time and price) for provisioning a cluster
 *
 * @author Hossein
 */
@Stateless
@Path("")
public class ClusterCostFacadeREST {

  @EJB
  private ClusterCost clusterCost;
  @EJB
  private MinCostInstanceEstimator minCostInstanceEstimator;

  private static final Logger logger = Logger.getLogger(ClusterCostFacadeREST.class);

  @POST
  @Path("cluster/cost")
  @Consumes({"text/plain"})
  @Produces({MediaType.APPLICATION_JSON})
  public ClusterTimePrice estimateAvailabilityTimeAndTrueCost(String yaml) throws KaramelException,
      ServiceRecommanderException {
    return clusterCost.estimateAvailabilityTimeAndTrueCost(yaml);
  }

  @POST
  @Path("profitinstance")
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public List<Ec2Instance> findAllInstancesZonesCost(InstanceSpecification instanceSpecification)
      throws ServiceRecommanderException {
    return minCostInstanceEstimator.findAllInstancesZonesCost(
        instanceSpecification.getAvailabilityTime(),
        instanceSpecification.getReliabilityLowerBound(),
        instanceSpecification.getMinECU(),
        instanceSpecification.getMinMemoryGB(),
        instanceSpecification.getMinStorage());
  }

}
