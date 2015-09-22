package se.kth.servicerecommender.json.amazon;

import java.util.List;

/**
 *
 * @author Hossein
 */
public class Region<T> {

  private String region;
  private List<T> instanceTypes;

  public Region() {
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public List<T> getInstanceTypes() {
    return instanceTypes;
  }

  public void setInstanceTypes(List<T> instanceTypes) {
    this.instanceTypes = instanceTypes;
  }

}
