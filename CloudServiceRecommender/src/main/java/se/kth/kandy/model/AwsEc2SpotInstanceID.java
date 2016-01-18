package se.kth.kandy.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Embeddable;

/**
 *
 * @author Hossein
 */
@Embeddable
public class AwsEc2SpotInstanceID implements Serializable {

  private static final long serialVersionUID = 1L;

  private String region;
  private String instanceType;
  private String productDescription;
  private Timestamp timeStamp;
  private String availabilityZone;

  public AwsEc2SpotInstanceID(String region, String instanceType, String productDescription, Timestamp timeStamp,
      String availabilityZone) {
    this.region = region;
    this.instanceType = instanceType;
    this.productDescription = productDescription;
    this.timeStamp = timeStamp;
    this.availabilityZone = availabilityZone;
  }

  public AwsEc2SpotInstanceID() {
  }

  public String getRegion() {
    return region;
  }

  public String getInstanceType() {
    return instanceType;
  }

  public String getProductDescription() {
    return productDescription;
  }

  public Timestamp getTimeStamp() {
    return timeStamp;
  }

  public String getAvailabilityZone() {
    return availabilityZone;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 37 * hash + Objects.hashCode(this.region);
    hash = 37 * hash + Objects.hashCode(this.instanceType);
    hash = 37 * hash + Objects.hashCode(this.productDescription);
    hash = 37 * hash + Objects.hashCode(this.timeStamp);
    hash = 37 * hash + Objects.hashCode(this.availabilityZone);
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
    final AwsEc2SpotInstanceID other = (AwsEc2SpotInstanceID) obj;
    if (!Objects.equals(this.region, other.region)) {
      return false;
    }
    if (!Objects.equals(this.instanceType, other.instanceType)) {
      return false;
    }
    if (!Objects.equals(this.productDescription, other.productDescription)) {
      return false;
    }
    if (!Objects.equals(this.timeStamp, other.timeStamp)) {
      return false;
    }
    if (!Objects.equals(this.availabilityZone, other.availabilityZone)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "AwsEc2SpotInstanceID{" + "region=" + region + ", instanceType=" + instanceType + ", productDescription="
        + productDescription + ", timeStamp=" + timeStamp + ", availabilityZone=" + availabilityZone + '}';
  }

}
