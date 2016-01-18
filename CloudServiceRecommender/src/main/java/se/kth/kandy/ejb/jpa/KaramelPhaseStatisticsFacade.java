package se.kth.kandy.ejb.jpa;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import se.kth.kandy.ejb.AbstractFacade;
import se.kth.kandy.model.KaramelPhaseStatistics;
import se.kth.kandy.model.KaramelStatistics;

/**
 *
 * @author Hossein
 */
@Stateless
public class KaramelPhaseStatisticsFacade extends AbstractFacade<KaramelPhaseStatistics> {

  @PersistenceContext(unitName = "ServiceRecommender-ejb_PU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public KaramelPhaseStatisticsFacade() {
    super(KaramelPhaseStatistics.class);
  }

  public long maxID(KaramelStatistics karamelStatistics) {
    Query query = getEntityManager().createNamedQuery("KaramelPhaseStatistics.maxID");
    query.setParameter("karamelStatisticsID", karamelStatistics.getId());
    if (query.getSingleResult() == null) {
      return 0;
    }
    return (Long) query.getSingleResult(); // if there would be no result it will return 0
  }
}
