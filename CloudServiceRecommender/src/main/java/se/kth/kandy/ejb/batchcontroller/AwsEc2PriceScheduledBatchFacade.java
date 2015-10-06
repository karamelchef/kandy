/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.ejb.batchcontroller;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import org.apache.log4j.Logger;
import se.kth.kandy.batch.executor.AwsEc2InstanceJob;
import static se.kth.kandy.batch.executor.AwsEc2InstanceJob.AWSEC2_INSTANCE_JOB;

/**
 * Enterprise java bean (Session bean). Provides business logic to start/stop batch job to fetch aws ec2 instances
 * prices from amazon periodically.
 *
 * @author Hossein
 */
@Stateless
public class AwsEc2PriceScheduledBatchFacade {

  /**
   * Delay between running the jobs
   */
  private static final int mDelay = 24;
  private static final Logger logger = Logger.getLogger(AwsEc2PriceScheduledBatchFacade.class);

  @Resource
  private ManagedScheduledExecutorService mExecutor;
  private ScheduledFuture<?> scheduledFuture;

  public AwsEc2PriceScheduledBatchFacade() {
  }

  public void runJob() {
    scheduledFuture = mExecutor.scheduleWithFixedDelay(new AwsEc2InstanceJob(), 0, mDelay, TimeUnit.HOURS);
  }

  public void stopJob() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
      logger.debug("Stop the job: " + AWSEC2_INSTANCE_JOB);
    }
  }

  public boolean isJobRunning() {
    if (scheduledFuture == null) {
      return false;
    }
    return !scheduledFuture.isCancelled();
  }
}
