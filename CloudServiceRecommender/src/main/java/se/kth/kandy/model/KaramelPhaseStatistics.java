package se.kth.kandy.model;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Hossein
 */
@Entity
@Table(name = "karamel_phase_statistics")
@NamedQuery(name = "KaramelPhaseStatistics.maxID",
    query = "SELECT max(k.id.id) FROM KaramelPhaseStatistics k WHERE k.id.karamelStatisticsId = :karamelStatisticsID")
public class KaramelPhaseStatistics implements Serializable {

  private static final long serialVersionUID = 1L;
  @EmbeddedId
  private KaramelPTStatisticsID id;
  private String name;
  private String status;
  private long duration;
  @MapsId("karamelStatisticsId")
  @ManyToOne
  @JoinColumn(name = "KARAMELSTATISTICS_ID")
  private KaramelStatistics karamelStatistics;

  public KaramelPhaseStatistics(KaramelPTStatisticsID id, String name, String status, long duration,
      KaramelStatistics karamelStatistics) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.duration = duration;
    this.karamelStatistics = karamelStatistics;
  }

  public KaramelPhaseStatistics() {
  }

  public KaramelPTStatisticsID getId() {
    return id;
  }

  public void setId(KaramelPTStatisticsID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public KaramelStatistics getKaramelStatistics() {
    return karamelStatistics;
  }

  public void setKaramelStatistics(KaramelStatistics karamelStatistics) {
    this.karamelStatistics = karamelStatistics;
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
    if (!(object instanceof KaramelPhaseStatistics)) {
      return false;
    }
    KaramelPhaseStatistics other = (KaramelPhaseStatistics) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "se.kth.kandy.model.KaramelPhaseStatistics[ id=" + id + " ]";
  }

}
