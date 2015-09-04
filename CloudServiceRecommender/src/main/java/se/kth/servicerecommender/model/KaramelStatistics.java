/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommender.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
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

  public KaramelStatistics() {
  }

  public KaramelStatistics(Date timeStamp, String statistics) {
    this.timeStamp = timeStamp;
    this.statistics = statistics;
  }

  public KaramelStatistics(Long id) {
    this.id = id;
  }

  public KaramelStatistics(Long id, Date timeStamp, String statistics) {
    this.id = id;
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
