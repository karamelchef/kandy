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
  public BigDecimal hourlyPrice; // For Ondemand instance price per hour for spot it is bid
  public INSTANCETYPE type;

  public Ec2Instance() {
  }

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
