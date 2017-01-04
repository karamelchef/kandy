package se.kth.kandy.experiments;

import java.math.BigDecimal;
import se.kth.kandy.ejb.algorithm.Ec2Instance;

/**
 *
 * @author Hossein
 */
public class InstanceZoneProfit extends InstanceZoneCost implements Comparable<InstanceZoneProfit> {

  @Override
  public int compareTo(InstanceZoneProfit o) {

    return this.estimatedProfit.compareTo(o.estimatedProfit);
  }

  public BigDecimal estimatedProfit;  // for all the hours, instance is running
  public Ec2Instance.INSTANCETYPE type;

  public InstanceZoneProfit(InstanceZoneCost object) {
    super(object);
  }

  public void setEstimatedProfit(BigDecimal estimatedProfit) {
    this.estimatedProfit = estimatedProfit;
  }

  public void setType(Ec2Instance.INSTANCETYPE type) {
    this.type = type;
  }

  public BigDecimal getEstimatedProfit() {
    return estimatedProfit;
  }

  public Ec2Instance.INSTANCETYPE getType() {
    return type;
  }

  @Override
  public String toString() {
    return super.toString() + " {" + "estimatedProfit=" + estimatedProfit + ", type=" + type + '}';
  }
}
