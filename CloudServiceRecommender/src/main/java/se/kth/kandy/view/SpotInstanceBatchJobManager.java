/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.view;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import se.kth.kandy.ejb.batchcontroller.SpotInstanceScheduledBatchFacade;

/**
 * JSF Managed Bean.
 *
 * @author Hossein
 */
@Named("spotInstanceBatchJobManager")
@SessionScoped
public class SpotInstanceBatchJobManager implements Serializable {

  @EJB
  private SpotInstanceScheduledBatchFacade spotInstanceScheduledBatchFacade;

  public void startFetchingSpotPrices() {
    spotInstanceScheduledBatchFacade.runJob();
  }

  public void stopFetchingSpotPrices() {
    spotInstanceScheduledBatchFacade.stopJob();
  }

  public boolean isBatchJobRunning() {
    return spotInstanceScheduledBatchFacade.isJobRunning();
  }

  public SpotInstanceBatchJobManager() {
  }

}
