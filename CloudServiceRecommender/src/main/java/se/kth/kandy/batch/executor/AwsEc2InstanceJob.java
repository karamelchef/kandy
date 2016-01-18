/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.batch.executor;

import java.util.Properties;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import org.apache.log4j.Logger;

/**
 *
 * @author Hossein
 */
public class AwsEc2InstanceJob implements Runnable {

  private static final Logger logger = Logger.getLogger(AwsEc2InstanceJob.class);
  public static final String AWSEC2_INSTANCE_JOB = "awsEc2InstanceJob";

  @Override
  public void run() {
    JobOperator jobOperator = BatchRuntime.getJobOperator();
    long id = jobOperator.start(AWSEC2_INSTANCE_JOB, new Properties());
    logger.debug("Start the job:  " + AWSEC2_INSTANCE_JOB + " with ID: " + id);
  }

}
