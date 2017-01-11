package se.kth.kandy.view;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.log4j.Logger;
import se.kth.kandy.ejb.batchcontroller.AwsEc2PriceScheduledBatchFacade;
import se.kth.kandy.ejb.jpa.KaramelStatisticsUtilFacade;
import se.kth.kandy.experiments.CostEstimationExperiment;
import se.kth.kandy.experiments.ProfitEstimationExperiment;

/**
 * JSF Managed Bean. Control panel for Kandy server
 *
 * @author Hossein
 */
@Named("controlPanel")
@ApplicationScoped
public class ControlPanel implements Serializable {

  private static final Logger logger = Logger.getLogger(ControlPanel.class);

  @EJB
  private KaramelStatisticsUtilFacade karamelStatisticsUtilFacade;
  @EJB
  private AwsEc2PriceScheduledBatchFacade awsEc2PriceScheduledBatchFacade;
  @EJB
  private CostEstimationExperiment costEstimationExperiment;
  @EJB
  private ProfitEstimationExperiment profitEstimationExperiment;

  private final String experiments = "1:Cost Estimation SameDataSet, 2:Cost/Profit Estimation DifferentDataset";

  private String experimentId;
  private int availabilityTimeHours;

  public void startFetchingSpotPrices() {
    awsEc2PriceScheduledBatchFacade.runJob();
  }

  public void stopFetchingSpotPrices() {
    awsEc2PriceScheduledBatchFacade.stopJob();
  }

  public boolean isBatchJobRunning() {
    return awsEc2PriceScheduledBatchFacade.isJobRunning();
  }

  public void parseStatistics() {
    karamelStatisticsUtilFacade.parseAndStoreStatistics();
  }

  public void runExperiment() throws Exception {

    Runnable r = new Runnable() { // make the call asynchronous, it is time consuming

      @Override
      public void run() {
        try {
          if (experimentId.equalsIgnoreCase("1")) {
            costEstimationExperiment.costEstimationEvaluation(availabilityTimeHours, null);
          }
          if (experimentId.equalsIgnoreCase("2")) {
            profitEstimationExperiment.costProfitEstimationEvaluation(availabilityTimeHours, null);
          }
        } catch (Exception ex) {
          logger.error(ex);
          logger.debug("stack trace --> ");
          ex.printStackTrace();
        }
      }
    };

    Thread t = new Thread(r);
    t.start();
    logger.debug("experiment submitted");
  }

  public ControlPanel() {
  }

  public void setExperimentId(String experimentId) {
    this.experimentId = experimentId;
  }

  public String getExperimentId() {
    return experimentId;
  }

  public String getExperiments() {
    return experiments;
  }

  public int getAvailabilityTimeHours() {
    return availabilityTimeHours;
  }

  public void setAvailabilityTimeHours(int availabilityTimeHours) {
    this.availabilityTimeHours = availabilityTimeHours;
  }

}
