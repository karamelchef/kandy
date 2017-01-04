package se.kth.kandy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity class used to store details of the spot instances fetched from amazon
 *
 * Prices are in $ per hour
 *
 * @author Hossein
 */
@Entity
@Table(name = "awsec2_spot_instance")
@NamedQueries({
  @NamedQuery(name = "AwsEc2SpotInstance.priceList",
      query = "SELECT k.price FROM AwsEc2SpotInstance k WHERE k.id.region = :region AND "
      + "k.id.instanceType = :instanceType AND k.id.productDescription = :productDescription "
      + "ORDER BY k.id.timeStamp DESC"),
  @NamedQuery(name = "AwsEc2SpotInstance.lastSamplingDate",
      query = "SELECT MAX(k.id.timeStamp) FROM AwsEc2SpotInstance k"),
  @NamedQuery(name = "AwsEc2SpotInstance.instancePriceList",
      query = "SELECT k FROM AwsEc2SpotInstance k WHERE k.id.instanceType = :instanceType AND "
      + "k.id.productDescription = :productDescription AND k.id.availabilityZone = :availabilityZone "
      + "ORDER BY k.id.timeStamp ASC"),
  @NamedQuery(name = "AwsEc2SpotInstance.instancePriceFilterdByTime",
      query = "SELECT k FROM AwsEc2SpotInstance k WHERE k.id.instanceType = :instanceType AND "
      + "k.id.productDescription = :productDescription AND k.id.availabilityZone = :availabilityZone "
      + "AND k.id.timeStamp >= :timeStamp ORDER BY k.id.timeStamp ASC"),
  @NamedQuery(name = "AwsEc2SpotInstance.availabilityZone",
      query = "SELECT distinct k.id.availabilityZone FROM AwsEc2SpotInstance k WHERE k.id.instanceType = :instanceType "
      + "AND k.id.productDescription = :productDescription")})
public class AwsEc2SpotInstance implements Serializable {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private AwsEc2SpotInstanceID id;
  @Column(precision = 10, scale = 4)
  private BigDecimal price;

  public AwsEc2SpotInstance(AwsEc2SpotInstanceID id, BigDecimal price) {
    this.id = id;
    this.price = price;
  }

  public AwsEc2SpotInstance() {
  }

  public AwsEc2SpotInstanceID getId() {
    return id;
  }

  public BigDecimal getPrice() {
    return price;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 53 * hash + Objects.hashCode(this.id);
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
    final AwsEc2SpotInstance other = (AwsEc2SpotInstance) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "AwsEc2SpotInstance{" + "id=" + id + ", price=" + price + '}';
  }

}
