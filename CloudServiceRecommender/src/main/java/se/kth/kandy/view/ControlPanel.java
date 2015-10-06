package se.kth.kandy.view;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import se.kth.kandy.ejb.batchcontroller.AwsEc2PriceScheduledBatchFacade;
import se.kth.kandy.ejb.jpa.KaramelStatisticsUtilFacade;

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

  public ControlPanel() {
  }

}
