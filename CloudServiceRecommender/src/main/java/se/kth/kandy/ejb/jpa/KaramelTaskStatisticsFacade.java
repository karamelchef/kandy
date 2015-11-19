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

  /**
   * Average time takes for running the task based on previously run tasks in database.
   *
   * Will return 0 if no history for similar task found in database
   *
   * @param taskId
   * @return
   */
  public long averageTaskTime(String taskId) {
    //TODO: Query can become more specific
    Query query = getEntityManager().createNamedQuery("KaramelTaskStatistics.averageTaskTime");
    query.setParameter("taskId", taskId);
    query.setParameter("status", "DONE");
    if (query.getSingleResult() == null) {
      return 0;
    }
    long duration;
    try {
      Double temp = (double) query.getSingleResult();
      duration = temp.longValue();
    } catch (ClassCastException e) {
      // for testing purpose and OpenEjb
      duration = (long) query.getSingleResult();
    }
    return duration;
  }

}
