package se.kth.kandy.batch;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;
import javax.batch.api.chunk.AbstractItemReader;
import javax.ejb.EJB;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.jclouds.aws.ec2.domain.Spot;
import org.jclouds.aws.ec2.options.DescribeSpotPriceHistoryOptions;
import se.kth.kandy.cloud.amazon.Ec2ApiWrapper;
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;

/**
 * Batch job artifact - fetch the spot instances details from amazon cloud
 *
 * Starts from the past even hour (0,2,4,..22) and make sampling for 1 hour and then goes back in time for STEP_LENGHT
 * hours and will do the same for past 85 days or last sampling date in database. everyday [23:00-00:00], [21:00-22:00],
 * [19:00-20:00]
 *
 * @author Hossein
 */
@Named
public class SpotInstanceItemReader extends AbstractItemReader {

  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;

  private static final Logger logger = Logger.getLogger(SpotInstanceItemReader.class);

  private Iterator<String> mRegions;
  private String mRegion;
  private Ec2ApiWrapper mEc2ApiWrapper;
  private static final String PRODUCT_DESCRIPTION = "Linux/UNIX";

  /**
   * use this time (mid night yesterday) to start fetching spot instance prices and goes back in time
   */
  private long mStartOfSamplingPeriod;
  /**
   * The wait time between starting new sampling
   */
  private static final long STEP_LENGHT = 7200000L;  // 2 hours
  /**
   * Length of each sampling
   */
  private static final long SAMPLING_LENGTH = 3600000L; // 1 hour
  public static final long SAMPLING_PERIOD_LENGHT = 7344000000L; // 85 days
  private long mSamplingTime;
  private long mEndTimeOfSamplingPeriod;

  public SpotInstanceItemReader() {
  }

  /**
   * Retrieve Spot prices for all instance types (ex : t1.micro), regions, product (Linux/UNIX) in
   *
   * @param checkpoint
   * @throws java.lang.Exception
   */
  @Override
  public void open(Serializable checkpoint) throws Exception {
    logger.info("Start fetching amazon Spot Instances");

    mEc2ApiWrapper = Ec2ApiWrapper.getInstance();

    mRegions = mEc2ApiWrapper.getConfiguredRegions().iterator();
    mRegion = mRegions.next();

    long now = new Date().getTime();

    Calendar calStart = new GregorianCalendar();
    calStart.setTime(new Date(now));
    calStart.set(Calendar.HOUR, 0);
    calStart.set(Calendar.MINUTE, 0);
    calStart.set(Calendar.SECOND, 0);
    calStart.set(Calendar.MILLISECOND, 0);

    long mod = (long) ((now - calStart.getTimeInMillis()) % STEP_LENGHT);

    mSamplingTime = now - mod;
    mStartOfSamplingPeriod = mSamplingTime;
    mEndTimeOfSamplingPeriod = mSamplingTime - SAMPLING_PERIOD_LENGHT;

    // fetch new data until last sampling date in database otherwise fetch data for past 85 days
    if (mEndTimeOfSamplingPeriod < awsEc2SpotInstanceFacade.getlastSamplingDate()) {
      mEndTimeOfSamplingPeriod = awsEc2SpotInstanceFacade.getlastSamplingDate();
    }
  }

  @Override
  public Set<Spot> readItem() throws Exception {
    if (mSamplingTime <= mEndTimeOfSamplingPeriod) {
      logger.debug("Aws Spot instance prices, fetched for region: " + mRegion);
      if (mRegions.hasNext()) {
        mRegion = mRegions.next();
        mSamplingTime = mStartOfSamplingPeriod;
      } else {
        return null; // end of sampling for all regions
      }
    }
    //Only approximately latest 960 records will be fetched
    Set<Spot> spots = mEc2ApiWrapper.getSpotPriceHistory(mRegion, DescribeSpotPriceHistoryOptions.Builder
        .from(new Date(mSamplingTime - SAMPLING_LENGTH)).to(new Date(mSamplingTime)).productDescription(
            PRODUCT_DESCRIPTION));
    mSamplingTime -= STEP_LENGHT;
    return spots;
  }
}
