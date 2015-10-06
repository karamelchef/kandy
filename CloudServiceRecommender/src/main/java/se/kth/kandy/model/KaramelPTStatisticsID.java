package se.kth.kandy.model;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * @author Hossein
 */
@Embeddable
public class KaramelPTStatisticsID implements Serializable {

  private long id;
  private long karamelStatisticsId;

  public KaramelPTStatisticsID(long id) {
    this.id = id;
  }

  public KaramelPTStatisticsID() {
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getKaramelStatisticsId() {
    return karamelStatisticsId;
  }

  public void setKaramelStatisticsId(long karamelStatisticsId) {
    this.karamelStatisticsId = karamelStatisticsId;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
    hash = 47 * hash + (int) (this.karamelStatisticsId ^ (this.karamelStatisticsId >>> 32));
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
    final KaramelPTStatisticsID other = (KaramelPTStatisticsID) obj;
    if (this.id != other.id) {
      return false;
    }
    if (this.karamelStatisticsId != other.karamelStatisticsId) {
      return false;
    }
    return true;
  }

}
