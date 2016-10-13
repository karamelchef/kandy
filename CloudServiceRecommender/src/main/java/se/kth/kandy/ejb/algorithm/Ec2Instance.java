package se.kth.kandy.ejb.algorithm;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * store Instance estimated cost/profit and bid
 *
 * @author Hossein
 */
@XmlRootElement
public class Ec2Instance implements Comparable<Ec2Instance>, Serializable {

  @Override
  public int compareTo(Ec2Instance o) {

    return this.estimatedCost.compareTo(o.estimatedCost);
  }

  public enum INSTANCETYPE {

    ONDEMAND, SPOT;
  }

  public String InstanceName;
  public String zone;
  public BigDecimal estimatedCost;  // for all the hours, instance is running
  public BigDecimal marketHourlyPrice; // Market price for both spot and ondemand
  public INSTANCETYPE type;
  public BigDecimal estimatedBid; // 0 for Ondemand

  public Ec2Instance() {
  }

  public Ec2Instance(String InstanceName, String zone, BigDecimal estimatedCost, BigDecimal marketHourlyPrice,
      INSTANCETYPE type, BigDecimal estimatedBid) {
    this.InstanceName = InstanceName;
    this.zone = zone;
    this.estimatedCost = estimatedCost;
    this.marketHourlyPrice = marketHourlyPrice;
    this.type = type;
    this.estimatedBid = estimatedBid;
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

  public BigDecimal getMarketHourlyPrice() {
    return marketHourlyPrice;
  }

  public BigDecimal getEstimatedBid() {
    return estimatedBid;
  }

  @Override
  public String toString() {
    return "{" + "InstanceName=" + InstanceName + ", zone=" + zone + ", estimatedCost=" + estimatedCost
        + ", marketHourlyPrice=" + marketHourlyPrice + ", type=" + type + ", estimatedBid=" + estimatedBid + '}';
  }

}
