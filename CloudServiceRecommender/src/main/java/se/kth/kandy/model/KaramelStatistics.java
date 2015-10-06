package se.kth.kandy.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Hossein
 */
@Entity
@XmlRootElement
@Table(name = "karamel_statistics")
public class KaramelStatistics implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  private Date timeStamp;

  @NotNull
  @Lob
  private String statistics;

  private String country;
  private String city;
  private String ip;

  public KaramelStatistics() {
  }

  public KaramelStatistics(Long id) {
    this.id = id;
  }

  /**
   * Used for update purposes
   *
   * @param id
   * @param timeStamp
   * @param statistics
   */
  public KaramelStatistics(Long id, Date timeStamp, String statistics) {
    this.id = id;
    this.timeStamp = timeStamp;
    this.statistics = statistics;
  }

  /**
   * Used for create purposes
   *
   * @param timeStamp
   * @param statistics
   */
  public KaramelStatistics(Date timeStamp, String statistics) {
    this.timeStamp = timeStamp;
    this.statistics = statistics;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getStatistics() {
    return statistics;
  }

  public void setStatistics(String statistics) {
    this.statistics = statistics;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
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
    if (!(object instanceof KaramelStatistics)) {
      return false;
    }
    KaramelStatistics other = (KaramelStatistics) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "se.kth.servicerecommander.ejb.Stat[ id=" + id + " ]";
  }

}
