package se.kth.kandy.ejb.jpa;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import se.kth.kandy.ejb.AbstractFacade;
import se.kth.kandy.model.KaramelStatistics;
import se.kth.kandy.model.KaramelTaskStatistics;

/**
 *
 * @author Hossein
 */
@Stateless
public class KaramelTaskStatisticsFacade extends AbstractFacade<KaramelTaskStatistics> {

  @PersistenceContext(unitName = "ServiceRecommender-ejb_PU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public KaramelTaskStatisticsFacade() {
    super(KaramelTaskStatistics.class);
  }

  public long maxID(KaramelStatistics karamelStatistics) {
    Query query = getEntityManager().createNamedQuery("KaramelTaskStatistics.maxID");
    query.setParameter("karamelStatisticsID", karamelStatistics.getId());
    return (Long) query.getSingleResult(); // if there would be no result it will return 0
  }

}
