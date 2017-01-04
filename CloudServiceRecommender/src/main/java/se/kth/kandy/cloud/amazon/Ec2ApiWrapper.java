package se.kth.kandy.cloud.amazon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jclouds.aws.ec2.domain.Spot;
import org.jclouds.aws.ec2.options.DescribeSpotPriceHistoryOptions;
import org.jclouds.ec2.features.SecurityGroupApi;
import org.jclouds.rest.AuthorizationException;
import se.kth.kandy.cloud.common.Ec2Credentials;
import se.kth.kandy.cloud.common.Settings;
import se.kth.kandy.cloud.common.SshKeyPair;
import se.kth.kandy.cloud.common.exception.InvalidEc2CredentialsException;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.cloud.util.CredentialsService;

/**
 * Wrapper for JClouds Apis to communicate with amazon
 *
 * Singleton design pattern used to generate an instance of this class.
 *
 * @author Hossein
 */
public class Ec2ApiWrapper {

  private static final Logger logger = Logger.getLogger(Ec2ApiWrapper.class);
  public Ec2Context context = null;
  public SshKeyPair sshKeyPair = null;
  private static Ec2ApiWrapper ec2ApiWrapper = null;

  private Ec2ApiWrapper() throws ServiceRecommanderException {
    CredentialsService credentialsHandling = new CredentialsService();
    sshKeyPair = credentialsHandling.loadSshKeysIfExist();
    if (sshKeyPair == null) {
      sshKeyPair = credentialsHandling.generateSshKeysAndUpdateConf();
    }
    Ec2Credentials credentials = credentialsHandling.loadEc2Credentials();
    this.context = validateCredentials(credentials);

    logger.info(String.format("Access-key='%s'", context.getCredentials().getAccessKey()));
    logger.info(String.format("Public-key='%s'", sshKeyPair.getPublicKeyPath()));
    logger.info(String.format("Private-key='%s'", sshKeyPair.getPrivateKeyPath()));
  }

  private Ec2Context validateCredentials(Ec2Credentials credentials) throws InvalidEc2CredentialsException {
    try {
      Ec2Context cxt = new Ec2Context(credentials);
      SecurityGroupApi securityGroupApi = cxt.getSecurityGroupApi();
      securityGroupApi.describeSecurityGroupsInRegion(Settings.PROVIDER_EC2_DEFAULT_REGION);
      return cxt;
    } catch (AuthorizationException e) {
      throw new InvalidEc2CredentialsException("accountid:" + credentials.getAccessKey(), e);
    }
  }

  public static Ec2ApiWrapper getInstance() throws ServiceRecommanderException {
    if (ec2ApiWrapper == null) {
      return new Ec2ApiWrapper();
    }
    return ec2ApiWrapper;
  }

  public Set<Spot> getSpotPriceHistory(String region,
      DescribeSpotPriceHistoryOptions options) {
    return context.getSpotInstanceApi().describeSpotPriceHistoryInRegion(region, options);
  }

  public BigDecimal getCurrentLinuxSpotPrice(String instanceType, final String availabilityZone) {
    String region = availabilityZone.substring(0, availabilityZone.length() - 1);
    Date now = new Date();
    List<Spot> spots = new ArrayList(getSpotPriceHistory(region, DescribeSpotPriceHistoryOptions.Builder.instanceType(
        instanceType).productDescription("Linux/UNIX").to(now).from(new Date(now.getTime() - 600000L))));

    /**
     * sort the spot list based on time descending and considers the availabilityZone. latest price for the availability
     * zone will be the first element
     */
    Collections.sort(spots, new Comparator<Spot>() {

      @Override
      public int compare(Spot o1, Spot o2) {
        if (o2.getAvailabilityZone().equals(availabilityZone)) {
          if (o1.getAvailabilityZone().equals(availabilityZone)) {
            if (o2.getTimestamp().compareTo(o1.getTimestamp()) == 1) {
              return 1;
            } else {
              return -1;
            }
          } else {
            return 1;
          }
        } else if (o1.getAvailabilityZone().equals(availabilityZone)) {
          return -1;
        } else {
          return 0;
        }
      }
    });
    if (spots.get(0).getAvailabilityZone().equals(availabilityZone)) {
      BigDecimal price = new BigDecimal(spots.get(0).getSpotPrice());
      price = price.setScale(4, RoundingMode.HALF_UP);
      return price;
    } else {
      return BigDecimal.ZERO;  // indicate error
    }
  }

  public Set<String> getConfiguredRegions() {
    return context.getConfiguredRegions();
  }
}
