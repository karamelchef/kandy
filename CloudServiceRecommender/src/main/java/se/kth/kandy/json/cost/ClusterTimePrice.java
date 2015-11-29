package se.kth.kandy.json.cost;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Json representation for a cluster running time and price
 *
 * @author Hossein
 */
public class ClusterTimePrice implements Serializable {

  private long duration;
  private BigDecimal price;

  public ClusterTimePrice() {
  }

  public ClusterTimePrice(long duration, BigDecimal price) {
    this.duration = duration;
    this.price = price;
  }

  public long getDuration() {
    return duration;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }
}
