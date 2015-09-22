package se.kth.servicerecommender.json.amazon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author Hossein
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueColumn {

  private String name;
  private Prices prices;

  public ValueColumn() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Prices getPrices() {
    return prices;
  }

  public void setPrices(Prices prices) {
    this.prices = prices;
  }

}
