/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommender.ejb.restservice;

import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import se.kth.servicerecommender.ejb.AbstractFacade;
import se.kth.servicerecommender.model.KaramelStatistics;

/**
 *
 * @author Hossein
 */
@Stateless
@Path("stats/cluster")
public class KaramelStatisticsFacadeREST extends AbstractFacade<KaramelStatistics> {

  @PersistenceContext(unitName = "ServiceRecommender-ejb_PU")
  private EntityManager em;

  public KaramelStatisticsFacadeREST() {
    super(KaramelStatistics.class);
  }

  @POST
  @Path("store")
  @Consumes({"text/plain"})
  @Produces("text/plain")
  public String create(String statistics) {
    KaramelStatistics karamelStatistics = super.create(new KaramelStatistics(new Date(), statistics));
    return String.valueOf(karamelStatistics.getId());
  }

  @POST
  @Path("update/{id}")
  @Consumes({"text/plain"})
  @Produces({"text/plain"})
  public String edit(@PathParam("id") Long id, String statistics) {
    KaramelStatistics karamelStatistics = super.edit(new KaramelStatistics(id, new Date(), statistics));
    return String.valueOf(karamelStatistics.getId());
  }

  @DELETE
  @Path("remove/{id}")
  public void remove(
      @PathParam("id") Long id) {
    super.remove(super.find(id));
  }

  @GET
  @Path("find/{id}")
  @Produces({"application/xml", "application/json"})
  public KaramelStatistics find(
      @PathParam("id") Long id) {
    return super.find(id);
  }

  @GET
  @Path("find/{from}/{to}")
  @Produces({"application/xml", "application/json"})
  public List<KaramelStatistics> findRange(
      @PathParam("from") Integer from,
      @PathParam("to") Integer to) {
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
