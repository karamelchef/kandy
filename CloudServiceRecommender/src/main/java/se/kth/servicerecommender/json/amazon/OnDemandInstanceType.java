package se.kth.servicerecommender.json.amazon;

import java.util.List;

/**
 *
 * @author Hossein
 */
public class OnDemandInstanceType {

  private String type;
  private List<OnDemandInstance> sizes;

  public OnDemandInstanceType() {
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<OnDemandInstance> getSizes() {
    return sizes;
  }

  public void setSizes(List<OnDemandInstance> sizes) {
    this.sizes = sizes;
  }

}
