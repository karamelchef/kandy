package se.kth.kandy.batch;

/**
 *
 * @author Hossein
 */
public class AwsEc2InstancesJsonPerOS {

  private final String json;
  private final String osType;

  public AwsEc2InstancesJsonPerOS(String json, String osType) {
    this.json = json;
    this.osType = osType;
  }

  public String getJson() {
    return json;
  }

  public String getOsType() {
    return osType;
  }

}
