/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@NamedQuery(name = "AwsEc2SpotInstance.priceList",
    query = "SELECT k.price FROM AwsEc2SpotInstance k WHERE k.region = :region AND "
    + "k.instanceType = :instanceType AND k.productDescription = :productDescription "
    + "ORDER BY k.timeStamp DESC")
public class AwsEc2SpotInstance implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String region;
  private String instanceType;
  private String productDescription;
  @Column(precision = 10, scale = 4)
  private BigDecimal price;
  private Timestamp timeStamp;
  private String availabilityZone;

  public AwsEc2SpotInstance(String region, String instanceType, String productDescription, BigDecimal price,
      Timestamp timeStamp, String availabilityZone) {
    this.region = region;
    this.instanceType = instanceType;
    this.productDescription = productDescription;
    this.price = price;
    this.timeStamp = timeStamp;
    this.availabilityZone = availabilityZone;
  }

  public AwsEc2SpotInstance() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getInstanceType() {
    return instanceType;
  }

  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  public String getProductDescription() {
    return productDescription;
  }

  public void setProductDescription(String productDescription) {
    this.productDescription = productDescription;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Timestamp getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Timestamp timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getAvailabilityZone() {
    return availabilityZone;
  }

  public void setAvailabilityZone(String availabilityZone) {
    this.availabilityZone = availabilityZone;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (id != null ? id.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof AwsEc2SpotInstance)) {
      return false;
    }
    AwsEc2SpotInstance other = (AwsEc2SpotInstance) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "se.kth.model.SpotInstance[ id=" + id + " ]";
  }

}
