package se.kth.kandy.ejb.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import se.kth.kandy.ejb.AbstractFacade;
import se.kth.kandy.ejb.notify.ServerPushFacade;
import se.kth.kandy.model.KaramelClusterStatistics;
import se.kth.kandy.model.KaramelPTStatisticsID;
import se.kth.kandy.model.KaramelPhaseStatistics;
import se.kth.kandy.model.KaramelStatistics;
import se.kth.kandy.model.KaramelTaskStatistics;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.stats.PhaseStat;
import se.kth.karamel.common.stats.TaskStat;

/**
 * Session bean for performing processes on {@link KaramelStatistics}
 *
 * @author Hossein
 */
@Stateless
public class KaramelStatisticsUtilFacade extends AbstractFacade<KaramelStatistics> {

  @EJB
  private ServerPushFacade serverPushFacade;

  @EJB
  private KaramelTaskStatisticsFacade karamelTaskStatisticsFacade;
  @EJB
  private KaramelPhaseStatisticsFacade karamelPhaseStatisticsFacade;
  @EJB
  private KaramelClusterStatisticsFacade karamelClusterStatisticsFacade;

  @PersistenceContext(unitName = "ServiceRecommender-ejb_PU")
  private EntityManager em;

  private static final Logger logger = Logger.getLogger(KaramelStatisticsUtilFacade.class);

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public KaramelStatisticsUtilFacade() {
    super(KaramelStatistics.class);
  }

  /**
   * Parse the statistics in {@link KaramelStatistics} and insert into specified tables. It is a manual way if tables
   * are unsynchronized.
   */
  public void parseAndStoreStatistics() {
    List<KaramelStatistics> table = super.findAll();
    for (KaramelStatistics karamelStatistics : table) {
      parseAndStoreStatistics(karamelStatistics, true);
    }
    String log = "All statistics parsed successfully";
    logger.info(log);
    serverPushFacade.pushLog("[ " + new Date() + " ] " + log);
  }

  /**
   * Parse an statistic received from a Karamel client and insert into specified database tables.
   *
   * If overwrite is false, just latest updates in cluster will be added to the database.
   *
   * @param karamelStatistics
   * @param overwrite
   */
  public void parseAndStoreStatistics(KaramelStatistics karamelStatistics, boolean overwrite) {

    ClusterStats clusterStats = parseKaramelStats(karamelStatistics.getStatistics());

    KaramelClusterStatistics karamelClusterStatistics = new KaramelClusterStatistics(karamelStatistics, clusterStats.
        getUserId(), clusterStats.getDefinition(), clusterStats.getStartTime(), clusterStats.getEndTime());
    karamelClusterStatisticsFacade.edit(karamelClusterStatistics);

    long maxId = karamelPhaseStatisticsFacade.maxID(karamelStatistics);
    for (PhaseStat phaseStat : clusterStats.getPhases()) {
      if (maxId == 0 || phaseStat.getId() > maxId || overwrite) {
        KaramelPhaseStatistics karamelPhaseStatistics = new KaramelPhaseStatistics(new KaramelPTStatisticsID(phaseStat.
            getId()), phaseStat.getName(), phaseStat.getStatus(), phaseStat.getDuration(), karamelStatistics);
        karamelPhaseStatisticsFacade.edit(karamelPhaseStatistics);
      }
    }

    maxId = karamelTaskStatisticsFacade.maxID(karamelStatistics);
    for (TaskStat taskStat : clusterStats.getTasks()) {
      if (maxId == 0 || taskStat.getId() > maxId || overwrite) {
        KaramelTaskStatistics karameltaskStatistics = new KaramelTaskStatistics(
            new KaramelPTStatisticsID(taskStat.getId()), taskStat.getTaskId(), taskStat.getMachineType(), taskStat.
            getStatus(), taskStat.getDuration(), karamelStatistics);
        karamelTaskStatisticsFacade.edit(karameltaskStatistics);
      }
    }
  }

  private ClusterStats parseKaramelStats(String json) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(json, ClusterStats.class);
    } catch (IOException ex) {
      logger.error("Problem in parsing statistics received from karamel");
    }
    return null;
  }
}
