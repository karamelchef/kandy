package se.kth.kandy.ejb.jpa;

import se.kth.kandy.ejb.AbstractFacade;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.kth.kandy.model.KaramelClusterStatistics;

/**
 *
 * @author Hossein
 */
@Stateless
public class KaramelClusterStatisticsFacade extends AbstractFacade<KaramelClusterStatistics> {

  @PersistenceContext(unitName = "ServiceRecommender-ejb_PU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public KaramelClusterStatisticsFacade() {
    super(KaramelClusterStatistics.class);
  }

}
