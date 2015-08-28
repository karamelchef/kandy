/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommander.view;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;
import se.kth.servicerecommander.ejb.batchcontroller.SpotInstanceScheduledBatchFacade;

/**
 * JSF Managed Bean.
 *
 * @author Hossein
 */
@Named("spotInstanceBatchJobManager")
@ConversationScoped
public class SpotInstanceBatchJobManager implements Serializable {

  @EJB
  private SpotInstanceScheduledBatchFacade spotInstanceScheduledBatchFacade;

  private boolean isBatchJobRunning = false;

  public void startFetchingSpotPrices() {
    spotInstanceScheduledBatchFacade.runJob();
    isBatchJobRunning = true;
  }

  public void stopFetchingSpotPrices() {
    spotInstanceScheduledBatchFacade.stopJob();
    isBatchJobRunning = false;
  }

  public boolean isBatchJobRunning() {
    return isBatchJobRunning;
  }

  public SpotInstanceBatchJobManager() {
  }

}
