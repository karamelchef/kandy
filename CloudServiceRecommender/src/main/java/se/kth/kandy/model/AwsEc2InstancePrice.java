package se.kth.kandy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity class for Aws Ec2 On demand and Reserved instance prices
 *
 * prices are in $ per hour
 *
 * @author Hossein
 */
@Entity
@Table(name = "awsec2_instance_price")
@NamedQueries({
  @NamedQuery(name = "AwsEc2InstancePrice.price",
      query = "SELECT a.price FROM AwsEc2InstancePrice a WHERE a.id.name = :instanceType AND "
      + "a.id.operatingSystem = :instanceOs AND a.id.purchaseOption = :purchaseOption AND a.id.region = :region"),
  @NamedQuery(name = "AwsEc2InstancePrice.region",
      query = "SELECT  distinct a.id.region FROM AwsEc2InstancePrice a WHERE a.id.name = :instanceType AND "
      + "a.id.operatingSystem = :instanceOs AND a.id.purchaseOption = :purchaseOption")})
public class AwsEc2InstancePrice implements Serializable {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private AwsEc2InstancePriceID id;
  @Column(precision = 10, scale = 4)
  private BigDecimal price;

  @MapsId("name") //specifies a foreign key for the composite primary key of this table
  @ManyToOne
  @JoinColumn(name = "AWSEC2INSTANCE_NAME")
  private AwsEc2Instance awsEc2Instance;

  public AwsEc2InstancePrice(AwsEc2InstancePriceID id, BigDecimal price, AwsEc2Instance awsEc2Instance) {
    this.id = id;
    this.price = price;
    this.awsEc2Instance = awsEc2Instance;
  }

  public AwsEc2InstancePrice() {
  }

  public AwsEc2InstancePriceID getId() {
    return id;
  }

  public void setId(AwsEc2InstancePriceID id) {
    this.id = id;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public AwsEc2Instance getAwsEc2Instance() {
    return awsEc2Instance;
  }

  public void setAwsEc2Instance(AwsEc2Instance awsEc2Instance) {
    this.awsEc2Instance = awsEc2Instance;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(this.id);
    hash = 37 * hash + Objects.hashCode(this.awsEc2Instance);
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
    final AwsEc2InstancePrice other = (AwsEc2InstancePrice) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.awsEc2Instance, other.awsEc2Instance)) {
      return false;
    }
    return true;
  }

}
