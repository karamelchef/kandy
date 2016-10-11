package se.kth.kandy.ejb.algorithm;

import java.math.BigDecimal;

/**
 * store Instance estimated cost/profit and bid
 *
 * @author Hossein
 */
public class Ec2Instance implements Comparable<Ec2Instance> {

  @Override
  public int compareTo(Ec2Instance o) {

    return this.estimatedCost.compareTo(o.estimatedCost);
  }

  public enum INSTANCETYPE {

    ONDEMAND, SPOT;
  }

  private String InstanceName;
  private String zone;
  private BigDecimal estimatedCost;  // for all the hours, instance is running
  private BigDecimal hourlyPrice; // For Ondemand instance price per hour for spot it is bid
  private INSTANCETYPE type;

  public Ec2Instance(String InstanceName, String zone, BigDecimal estimatedCost, INSTANCETYPE type,
      BigDecimal hourlyPrice) {
    this.InstanceName = InstanceName;
    this.zone = zone;
    this.estimatedCost = estimatedCost;
    this.type = type;
    this.hourlyPrice = hourlyPrice;
  }

  public String getInstanceName() {
    return InstanceName;
  }

  public String getZone() {
    return zone;
  }

  public BigDecimal getEstimatedCost() {
    return estimatedCost;
  }

  public INSTANCETYPE getType() {
    return type;
  }

  public BigDecimal getHourlyPrice() {
    return hourlyPrice;
  }

  @Override
  public String toString() {
    return "{" + "InstanceName=" + InstanceName + ", zone=" + zone + ", estimatedCost=" + estimatedCost
        + ", hourlyPrice=" + hourlyPrice + ", type=" + type + '}';
  }

}
