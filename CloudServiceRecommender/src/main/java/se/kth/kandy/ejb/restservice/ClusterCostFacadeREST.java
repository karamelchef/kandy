package se.kth.kandy.ejb.restservice;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.log4j.Logger;
import se.kth.karamel.common.exception.KaramelException;

/**
 * JaxRS rest service implemented as EJB.
 *
 * Calculates expected cost(time and price) for provisioning a cluster
 *
 * @author Hossein
 */
@Stateless
@Path("cluster/cost")
public class ClusterCostFacadeREST {

  @EJB
  private ClusterCost clusterCost;

  private static final Logger logger = Logger.getLogger(ClusterCostFacadeREST.class);

  @POST
  @Path("time")
  @Consumes({"text/plain"})
  @Produces("text/plain")
  public String calculateTime(String yaml) throws KaramelException {
    long clusterTime = clusterCost.getClusterTime(yaml);
    logger.debug("Cluster time in milisecond: " + clusterTime);
    return String.valueOf(clusterTime);
  }

  @POST
  @Path("price")
  @Consumes({"text/plain"})
  @Produces("text/plain")
  public String calculatePrice(String yaml) {
    return null;
  }

}
