package se.kth.kandy.model;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Hossein
 */
@Entity
@Table(name = "karamel_task_statistics")
@NamedQueries({
  @NamedQuery(name = "KaramelTaskStatistics.maxID",
      query = "SELECT max(k.id.id) FROM KaramelTaskStatistics k WHERE k.id.karamelStatisticsId = :karamelStatisticsID"),
  @NamedQuery(name = "KaramelTaskStatistics.averageTaskTime",
      query = "SELECT AVG(k.duration) FROM KaramelTaskStatistics k WHERE k.taskId = :taskId AND k.status = :status "
      + "AND k.machineType LIKE :machineType")})
public class KaramelTaskStatistics implements Serializable {

  private static final long serialVersionUID = 1L;
  @EmbeddedId
  private KaramelPTStatisticsID id;
  private String taskId;
  private String machineType;
  private String status;
  private long duration;
  @MapsId("karamelStatisticsId")
  @ManyToOne
  @JoinColumn(name = "KARAMELSTATISTICS_ID")
  private KaramelStatistics karamelStatistics;

  public KaramelTaskStatistics(KaramelPTStatisticsID id, String taskId, String machineType, String status,
      long duration, KaramelStatistics karamelStatistics) {
    this.id = id;
    this.taskId = taskId;
    this.machineType = machineType;
    this.status = status;
    this.duration = duration;
    this.karamelStatistics = karamelStatistics;
  }

  public KaramelTaskStatistics() {
  }

  public KaramelPTStatisticsID getId() {
    return id;
  }

  public void setId(KaramelPTStatisticsID id) {
    this.id = id;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getMachineType() {
    return machineType;
  }

  public void setMachineType(String machineType) {
    this.machineType = machineType;
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
    if (!(object instanceof KaramelTaskStatistics)) {
      return false;
    }
    KaramelTaskStatistics other = (KaramelTaskStatistics) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "se.kth.kandy.model.KaramelTaskStatistics[ id=" + id + " ]";
  }

}
