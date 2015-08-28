/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommander.batch.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.batch.runtime.BatchRuntime;
import org.apache.log4j.Logger;

/**
 *
 * @author Hossein
 */
public class SpotInstanceJob implements Runnable {

  public static List<Long> mExecutedBatchs = new ArrayList<>();
  private static final Logger logger = Logger.getLogger(SpotInstanceJob.class);
  public static final String SPOT_INSTANCE_JOB = "spotInstanceJob";

  @Override
  public void run() {
    logger.debug("Start the job:  " + SPOT_INSTANCE_JOB);
    mExecutedBatchs.add(BatchRuntime.getJobOperator().start(SPOT_INSTANCE_JOB, new Properties()));
  }

}
