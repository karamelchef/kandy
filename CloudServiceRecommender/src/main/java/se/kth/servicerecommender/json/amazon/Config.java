package se.kth.servicerecommender.json.amazon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 *
 * @author Hossein
 * @param <T>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config<T> {

  private List<String> valueColumns;
  private List<String> currencies;
  private List<Region<T>> regions;

  public Config() {
  }

  public List<String> getValueColumns() {
    return valueColumns;
  }

  public void setValueColumns(List<String> valueColumns) {
    this.valueColumns = valueColumns;
  }

  public List<String> getCurrencies() {
    return currencies;
  }

  public void setCurrencies(List<String> currencies) {
    this.currencies = currencies;
  }

  public List<Region<T>> getRegions() {
    return regions;
  }

  public void setRegions(List<Region<T>> regions) {
    this.regions = regions;
  }

}
