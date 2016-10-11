package se.kth.kandy.ejb.restservice;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.log4j.Logger;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.algorithm.ClusterCost;
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
@Path("cluster")
public class ClusterCostFacadeREST {

  @EJB
  private ClusterCost clusterCost;

  private static final Logger logger = Logger.getLogger(ClusterCostFacadeREST.class);

  @POST
  @Path("cost")
  @Consumes({"text/plain"})
  @Produces({"application/json"})
  public ClusterTimePrice calculateCost(String yaml) throws KaramelException, ServiceRecommanderException {
    return clusterCost.estimateAvailabilityTimeAndTrueCost(yaml);
  }
}
