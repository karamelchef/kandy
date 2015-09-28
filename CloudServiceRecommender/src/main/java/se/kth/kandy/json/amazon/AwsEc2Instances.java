package se.kth.kandy.json.amazon;

/**
 *
 * @author Hossein
 * @param <T>
 */
public class AwsEc2Instances<T> {

  private float vers;
  private Config<T> config;

  public AwsEc2Instances() {
  }

  public float getVers() {
    return vers;
  }

  public void setVers(float vers) {
    this.vers = vers;
  }

  public Config<T> getConfig() {
    return config;
  }

  public void setConfig(Config<T> config) {
    this.config = config;
  }

}
