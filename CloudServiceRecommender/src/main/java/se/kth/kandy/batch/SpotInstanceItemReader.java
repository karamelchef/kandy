/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.batch;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import javax.batch.api.chunk.AbstractItemReader;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.jclouds.aws.ec2.domain.Spot;
import org.jclouds.aws.ec2.options.DescribeSpotPriceHistoryOptions;
import se.kth.kandy.cloud.amazon.Ec2ApiWrapper;
import se.kth.kandy.cloud.common.Ec2Credentials;
import se.kth.kandy.cloud.common.SshKeyPair;
import se.kth.kandy.cloud.util.CredentialsService;

/**
 * Batch job artifact - read the spot instances details from amazon cloud
 *
 * @author Hossein
 */
@Named
public class SpotInstanceItemReader extends AbstractItemReader {

  private static final Logger logger = Logger.getLogger(SpotInstanceItemReader.class);

  private Iterator<Spot> mSpots;

  public SpotInstanceItemReader() {
  }

  /**
   * Retrieve Spot prices for all instance types (ex : t1.micro) and products (SUSE Linux (Amazon VPC)) in the past
   * minute
   *
   * @param checkpoint
   * @throws java.lang.Exception
   */
  @Override
  public void open(Serializable checkpoint) throws Exception {
    logger.info("Start fetching amazon Spot Instances");
    CredentialsService credentialsHandling = new CredentialsService();
    SshKeyPair sshKeyPair = credentialsHandling.loadSshKeysIfExist();
    if (sshKeyPair == null) {
      sshKeyPair = credentialsHandling.generateSshKeysAndUpdateConf();
    }
    Ec2Credentials credentials = credentialsHandling.loadEc2Credentials();

    Ec2ApiWrapper ec2ApiWrapper = new Ec2ApiWrapper(Ec2ApiWrapper.validateCredentials(credentials), sshKeyPair);
    long timeStamp = System.currentTimeMillis();
    Date from = new Date(timeStamp - 3600000); // 1 hour ago
    Date to = new Date(timeStamp);
    //Only approximately latest 960 records will be fetched
    Set<Spot> spots = ec2ApiWrapper.getSpotPriceHistory(null, DescribeSpotPriceHistoryOptions.Builder
        .from(from)
        .to(to));
    mSpots = spots.iterator();
    String log = spots.size() + " Aws Spot instance prices in the past 1 hour, fetched. ";
    logger.info(log);
  }

  @Override
  public Spot readItem() throws Exception {
    if (mSpots.hasNext()) {
      return mSpots.next();
    }
    return null;
  }
}