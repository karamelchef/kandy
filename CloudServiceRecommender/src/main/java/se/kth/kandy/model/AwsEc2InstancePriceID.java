package se.kth.kandy.model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

/**
 *
 * @author Hossein
 */
@Embeddable
public class AwsEc2InstancePriceID implements Serializable {

  private String name;
  private String region;
  private String operatingSystem;
  private String purchaseOption;

  public AwsEc2InstancePriceID(String region, String operatingSystem, String purchaseOption) {
    this.region = region;
    this.operatingSystem = operatingSystem;
    this.purchaseOption = purchaseOption;
  }

  public AwsEc2InstancePriceID() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getOperatingSystem() {
    return operatingSystem;
  }

  public void setOperatingSystem(String operatingSystem) {
    this.operatingSystem = operatingSystem;
  }

  public String getPurchaseOption() {
    return purchaseOption;
  }

  public void setPurchaseOption(String purchaseOption) {
    this.purchaseOption = purchaseOption;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + Objects.hashCode(this.name);
    hash = 67 * hash + Objects.hashCode(this.region);
    hash = 67 * hash + Objects.hashCode(this.operatingSystem);
    hash = 67 * hash + Objects.hashCode(this.purchaseOption);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AwsEc2InstancePriceID other = (AwsEc2InstancePriceID) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.region, other.region)) {
      return false;
    }
    if (!Objects.equals(this.operatingSystem, other.operatingSystem)) {
      return false;
    }
    if (!Objects.equals(this.purchaseOption, other.purchaseOption)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "AwsEc2InstancePrice{" + "name=" + name + ", region=" + region + ", operatingSystem="
        + operatingSystem + ", purchaseOption=" + purchaseOption + '}';
  }

}
