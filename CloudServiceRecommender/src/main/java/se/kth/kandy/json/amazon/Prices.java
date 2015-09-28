package se.kth.kandy.json.amazon;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Hossein
 */
public class Prices {

  @JsonProperty("USD")
  private String USD;

  public Prices() {
  }

  public String getUSD() {
    return USD;
  }

  public void setUSD(String USD) {
    this.USD = USD;
  }
}
