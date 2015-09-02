/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommender.ejb.batchcontroller;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import org.apache.log4j.Logger;
import se.kth.servicerecommender.batch.executor.SpotInstanceJob;
import static se.kth.servicerecommender.batch.executor.SpotInstanceJob.SPOT_INSTANCE_JOB;

/**
 * Enterprise java bean (Session bean). Provides business logic to start/stop batch job to fetch spot instances prices
 * from amazon periodically.
 *
 * @author Hossein
 */
@Stateless
public class SpotInstanceScheduledBatchFacade {

  /**
   * Delay between running the jobs
   */
  private static final int mDelay = 360;
  private static final Logger logger = Logger.getLogger(SpotInstanceScheduledBatchFacade.class);

  @Resource
  private ManagedScheduledExecutorService mExecutor;
  private ScheduledFuture<?> scheduledFuture;

  public SpotInstanceScheduledBatchFacade() {
  }

  public void runJob() {
    scheduledFuture = mExecutor.scheduleWithFixedDelay(new SpotInstanceJob(), 0, mDelay, TimeUnit.SECONDS);
  }

  public void stopJob() {
    scheduledFuture.cancel(true);
    logger.debug("Stop the job: " + SPOT_INSTANCE_JOB);

  }
}
