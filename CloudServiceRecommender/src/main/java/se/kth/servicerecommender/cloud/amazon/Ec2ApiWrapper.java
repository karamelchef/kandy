/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommender.cloud.amazon;

import java.util.Set;
import org.apache.log4j.Logger;
import org.jclouds.aws.ec2.domain.Spot;
import org.jclouds.aws.ec2.options.DescribeSpotPriceHistoryOptions;
import org.jclouds.ec2.features.SecurityGroupApi;
import org.jclouds.rest.AuthorizationException;
import se.kth.servicerecommender.cloud.common.Ec2Credentials;
import se.kth.servicerecommender.cloud.common.Settings;
import se.kth.servicerecommender.cloud.common.SshKeyPair;
import se.kth.servicerecommender.cloud.common.exception.InvalidEc2CredentialsException;

/**
 *
 * @author Hossein
 */
public class Ec2ApiWrapper {

  private static final Logger logger = Logger.getLogger(Ec2ApiWrapper.class);
  public final Ec2Context context;
  public final SshKeyPair sshKeyPair;

  public Ec2ApiWrapper(Ec2Context context, SshKeyPair sshKeyPair) {
    this.context = context;
    this.sshKeyPair = sshKeyPair;
    logger.info(String.format("Access-key='%s'", context.getCredentials().getAccessKey()));
    logger.info(String.format("Public-key='%s'", sshKeyPair.getPublicKeyPath()));
    logger.info(String.format("Private-key='%s'", sshKeyPair.getPrivateKeyPath()));
  }

  public static Ec2Context validateCredentials(Ec2Credentials credentials) throws InvalidEc2CredentialsException {
    try {
      Ec2Context cxt = new Ec2Context(credentials);
      SecurityGroupApi securityGroupApi = cxt.getSecurityGroupApi();
      securityGroupApi.describeSecurityGroupsInRegion(Settings.PROVIDER_EC2_DEFAULT_REGION);
      return cxt;
    } catch (AuthorizationException e) {
      throw new InvalidEc2CredentialsException("accountid:" + credentials.getAccessKey(), e);
    }
  }

  public Set<Spot> getSpotPriceHistory(String region,
      DescribeSpotPriceHistoryOptions options) {
    return context.getSpotInstanceApi().describeSpotPriceHistoryInRegion(region, options);
  }
}
