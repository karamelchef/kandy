package se.kth.kandy.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Hossein
 */
@Entity
@Table(name = "karamel_cluster_statistics")
public class KaramelClusterStatistics implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @OneToOne
  private KaramelStatistics karamelStatistics;
  private String userID;
  @NotNull
  @Lob
  private String definition;
  private long startTime;
  private long endTime;

  public KaramelClusterStatistics(KaramelStatistics karamelStatistics, String userID, String definition, long startTime,
      long endTime) {
    this.karamelStatistics = karamelStatistics;
    this.userID = userID;
    this.definition = definition;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public KaramelClusterStatistics() {
  }

  public KaramelStatistics getKaramelStatistics() {
    return karamelStatistics;
  }

  public void setKaramelStatistics(KaramelStatistics karamelStatistics) {
    this.karamelStatistics = karamelStatistics;
  }

  public String getUserID() {
    return userID;
  }

  public void setUserID(String userID) {
    this.userID = userID;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (karamelStatistics.getId() != null ? karamelStatistics.getId().hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof KaramelClusterStatistics)) {
      return false;
    }
    KaramelClusterStatistics other = (KaramelClusterStatistics) object;
    if ((this.karamelStatistics.getId() == null && other.karamelStatistics.getId() != null) || (this.karamelStatistics.
        getId() != null && !this.karamelStatistics.getId().equals(other.karamelStatistics.getId()))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "se.kth.kandy.model.KaramelStatisticsParsed[ id=" + karamelStatistics.getId() + " ]";
  }

}
