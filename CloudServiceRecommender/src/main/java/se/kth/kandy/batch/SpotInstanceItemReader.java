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
 * Starts from midnight today, 00:00:00 and makes sampling for 1 hour and then goes back in time for 8 hours and will do
 * the same for past 60 days or last sampling date in database. everyday [23:00-00:00], [15:00-16:00] and [07:00-08:00]
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
  private Date mMidnightYesterday;
  /**
   * The wait time between starting new sampling
   */
  private static final long STEP_LENGHT = 28800000L;  // 8 hours
  /**
   * Length of each sampling
   */
  private static final long SAMPLING_LENGTH = 3600000L; // 1 hour
  public static final long SAMPLING_PERIOD_LENGHT = 5184000000L; // 2 months
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

    Calendar calStart = new GregorianCalendar();
    calStart.setTime(new Date());
    calStart.set(Calendar.HOUR_OF_DAY, 0);
    calStart.set(Calendar.MINUTE, 0);
    calStart.set(Calendar.SECOND, 0);
    calStart.set(Calendar.MILLISECOND, 0);
    mMidnightYesterday = calStart.getTime();

    mSamplingTime = mMidnightYesterday.getTime();
    mEndTimeOfSamplingPeriod = mSamplingTime - SAMPLING_PERIOD_LENGHT;

    // fetch new data until last sampling date in database otherwise fetch data for past two months
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
        mSamplingTime = mMidnightYesterday.getTime();
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
