package se.kth.kandy.ejb.restservice;

import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import org.apache.log4j.Logger;
import se.kth.kandy.ejb.AbstractFacade;
import se.kth.kandy.ejb.jpa.KaramelStatisticsUtilFacade;
import se.kth.kandy.json.geo.GeoLocation;
import se.kth.kandy.model.KaramelStatistics;

/**
 * JaxRS rest service implemented as EJB.
 *
 * Process statistics received from Karamel clients.
 *
 * @author Hossein
 */
@Stateless
@Path("stats/cluster")
public class KaramelStatisticsFacadeREST extends AbstractFacade<KaramelStatistics> {

  @EJB
  private KaramelStatisticsUtilFacade karamelStatisticsUtilFacade;

  private static final Logger logger = Logger.getLogger(KaramelStatisticsFacadeREST.class);

  @PersistenceContext(unitName = "ServiceRecommender-ejb_PU")
  private EntityManager em;

  public KaramelStatisticsFacadeREST() {
    super(KaramelStatistics.class);
  }

  @POST
  @Path("store")
  @Consumes({"text/plain"})
  @Produces("text/plain")
  public String create(String statistics, @Context HttpServletRequest requestContext) {
    KaramelStatistics karamelStatistics = new KaramelStatistics(new Date(), statistics);
    String ip = requestContext.getRemoteAddr();
    karamelStatistics.setIp(ip);
    try {
      GeoLocation geoLocation = HostIpJerssyClient.mapToLocation(ip);
      karamelStatistics.setCity(geoLocation.getCity());
      karamelStatistics.setCountry(geoLocation.getCountry_name());
    } catch (Exception ex) {
      logger.error("Problem in retriving client location for IP: " + ip);
    }
    karamelStatistics = super.create(karamelStatistics);
    karamelStatisticsUtilFacade.parseAndStoreStatistics(karamelStatistics, false);
    return String.valueOf(karamelStatistics.getId());
  }

  @POST
  @Path("update/{id}")
  @Consumes({"text/plain"})
  @Produces({"text/plain"})
  public String edit(@PathParam("id") Long id, String statistics, @Context HttpServletRequest requestContext) {
    KaramelStatistics karamelStatistics = new KaramelStatistics(id, new Date(), statistics);
    String ip = requestContext.getRemoteAddr();
    karamelStatistics.setIp(ip);
    try {
      GeoLocation geoLocation = HostIpJerssyClient.mapToLocation(ip);
      karamelStatistics.setCity(geoLocation.getCity());
      karamelStatistics.setCountry(geoLocation.getCountry_name());
    } catch (Exception ex) {
      logger.error("Problem in retriving client location for IP: " + ip);
    }
    karamelStatistics = super.edit(karamelStatistics);
    karamelStatisticsUtilFacade.parseAndStoreStatistics(karamelStatistics, false);
    return String.valueOf(karamelStatistics.getId());
  }

  @DELETE
  @Path("remove/{id}")
  public void remove(
      @PathParam("id") Long id
  ) {
    super.remove(super.find(id));
  }

  @GET
  @Path("find/{id}")
  @Produces({"application/xml", "application/json"})
  public KaramelStatistics find(
      @PathParam("id") Long id
  ) {
    return super.find(id);
  }

  @GET
  @Path("find/{from}/{to}")
  @Produces({"application/xml", "application/json"})
  public List<KaramelStatistics> findRange(
      @PathParam("from") Integer from,
      @PathParam("to") Integer to
  ) {
    return super.findRange(new int[]{from, to});
  }

  @GET
  @Path("count")
  @Produces("text/plain")
  public String countREST() {
    return String.valueOf(super.count());
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }
}
