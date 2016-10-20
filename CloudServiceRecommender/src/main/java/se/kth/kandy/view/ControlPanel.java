package se.kth.kandy.view;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import se.kth.kandy.ejb.batchcontroller.AwsEc2PriceScheduledBatchFacade;
import se.kth.kandy.ejb.jpa.KaramelStatisticsUtilFacade;
import se.kth.kandy.experiments.CostEstimationExperiment;

/**
 * JSF Managed Bean. Control panel for Kandy server
 *
 * @author Hossein
 */
@Named("controlPanel")
@SessionScoped
public class ControlPanel implements Serializable {

  @EJB
  private KaramelStatisticsUtilFacade karamelStatisticsUtilFacade;
  @EJB
  private AwsEc2PriceScheduledBatchFacade awsEc2PriceScheduledBatchFacade;
  @EJB
  private CostEstimationExperiment costEstimationExperiment;

  private final String experiments = "1:CostEstimation";

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
    if (experimentId.equalsIgnoreCase("1")) {
      costEstimationExperiment.costEstimationEvaluation(availabilityTimeHours);
    }
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
