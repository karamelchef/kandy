package se.kth.kandy.experiments;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 *
 * @author Hossein
 */
public class InstanceZoneCost {

  protected String InstanceName;
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

  public InstanceZoneCost() {
  }

  public InstanceZoneCost(String InstanceName, String zone, long availabilityTime, BigDecimal marketHourlyPrice,
      long startTime, float reliability, BigDecimal bid) {
    this.InstanceName = InstanceName;
    this.zone = zone;
    this.availabilityTime = availabilityTime;
    this.marketHourlyPrice = marketHourlyPrice;
    this.startTime = startTime;
    this.reliability = reliability;
    this.bid = bid;
  }

  /**
   * used for profit experiment
   *
   * @param object
   */
  protected InstanceZoneCost(InstanceZoneCost object) {
    this.InstanceName = object.InstanceName;
    this.zone = object.zone;
    this.availabilityTime = object.availabilityTime;
    this.marketHourlyPrice = object.marketHourlyPrice;
    this.startTime = object.startTime;
    this.reliability = object.reliability;
    this.bid = object.bid;
    this.success = object.success;
    this.trueCost = object.trueCost;
    this.estimatedCost = object.estimatedCost;
    this.percentRelativeError = object.percentRelativeError;
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
   * @param runTime
   */
  public void calculateEstimatedCost(double runTime) {
    this.estimatedCost = marketHourlyPrice.multiply(new BigDecimal(runTime)).setScale(4, RoundingMode.HALF_UP);
  }

  public void setEstimatedCost(BigDecimal estimatedCost) {
    this.estimatedCost = estimatedCost;
  }

  /**
   * Sums up each hour market price to make the final true cost
   *
   * @param anHourPrice
   */
  public void addTrueCost(BigDecimal anHourPrice) {
    this.trueCost = this.trueCost.add(anHourPrice);
  }

  public void setTrueCost(BigDecimal trueCost) {
    this.trueCost = trueCost;
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

  public String getInstanceName() {
    return InstanceName;
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
    return "{" + "i=" + InstanceName + ", z=" + zone + ", Tr=" + availabilityTime + ", slb=" + reliability
        + ", startTime=" + new Date(startTime) + ", Pmkt=" + marketHourlyPrice + ", bid=" + bid + ", success=" + success
        + ", estimatedCost=" + estimatedCost + ", trueCost=" + trueCost
        + ", PRE=" + percentRelativeError + '}';
  }

}
