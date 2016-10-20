package se.kth.kandy.experiments;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 *
 * @author Hossein
 */
public class InstanceZoneCost {

  protected String InstanceType;
  protected String zone;
  protected long availabilityTime;
  protected float reliability;
  protected long startTime;
  protected BigDecimal marketHourlyPrice;
  protected BigDecimal bid;

  protected boolean success;
  protected BigDecimal estimatedCost = BigDecimal.ZERO;
  protected BigDecimal trueCost = BigDecimal.ZERO;
  protected BigDecimal percentRelativeError = BigDecimal.ZERO;

  public InstanceZoneCost(String InstanceType, String zone, long availabilityTime, BigDecimal marketHourlyPrice,
      long startTime, float reliability, BigDecimal bid) {
    this.InstanceType = InstanceType;
    this.zone = zone;
    this.availabilityTime = availabilityTime;
    this.marketHourlyPrice = marketHourlyPrice;
    this.startTime = startTime;
    this.reliability = reliability;
    this.bid = bid;
  }

  /**
   * set instance success or failure(termination)
   *
   * @param success
   */
  public void setSuccess(boolean success) {
    this.success = success;
  }

  /**
   * Calculates the estimated cost in case of success or failure(termination)
   *
   * estimatedCost = Pmkt * T
   *
   * T = ceiling ( availabilityTime) //in case of success
   *
   * T = average run time before termination //in case failure
   *
   * @param availabilityTime
   */
  public void calculateEstimatedCost(double availabilityTime) {
    this.estimatedCost = marketHourlyPrice.multiply(new BigDecimal(availabilityTime)).setScale(4, RoundingMode.HALF_UP);
  }

  /**
   * Sums up each hour market price to make the final true cost
   *
   * @param anHourPrice
   */
  public void addTrueCost(BigDecimal anHourPrice) {
    this.trueCost = this.trueCost.add(anHourPrice);
  }

  /**
   * calculates percent relative error, in case of success or failure
   *
   * percentRelativeError = ( |trueCost - estimatedCost| / trueCost ) * 100
   *
   * result is in percent
   */
  public void calculatePercentRelativeError() {
    if (trueCost.compareTo(BigDecimal.ZERO) == 1) {
      /*if true cost is zero, it means instance was terminated right at starting point, since bid was lower than market
       price, we do not take in to simulation these type of instances */
      this.percentRelativeError = trueCost.subtract(estimatedCost).abs().divide(trueCost, 3, RoundingMode.HALF_UP).
          multiply(new BigDecimal(100));
    }
  }

  public String getInstanceType() {
    return InstanceType;
  }

  public String getZone() {
    return zone;
  }

  public long getAvailabilityTime() {
    return availabilityTime;
  }

  public BigDecimal getMarketHourlyPrice() {
    return marketHourlyPrice;
  }

  public long getStartTime() {
    return startTime;
  }

  public boolean isSuccess() {
    return success;
  }

  public BigDecimal getEstimatedCost() {
    return estimatedCost;
  }

  public BigDecimal getTrueCost() {
    return trueCost;
  }

  public float getReliability() {
    return reliability;
  }

  public BigDecimal getBid() {
    return bid;
  }

  public BigDecimal getPercentRelativeError() {
    return percentRelativeError;
  }

  @Override
  public String toString() {
    return "{" + "i=" + InstanceType + ", z=" + zone + ", Tr=" + availabilityTime + ", slb=" + reliability
        + ", startTime=" + new Date(startTime) + ", Pmkt=" + marketHourlyPrice + ", bid=" + bid + ", success=" + success
        + ", estimatedCost=" + estimatedCost + ", trueCost=" + trueCost
        + ", percentRelativeError=" + percentRelativeError + '}';
  }

}
