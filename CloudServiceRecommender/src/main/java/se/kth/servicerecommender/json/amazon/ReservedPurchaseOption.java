package se.kth.servicerecommender.json.amazon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 *
 * @author Hossein
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservedPurchaseOption {

  private String purchaseOption;
  private List<ValueColumn> valueColumns;
  private Prices prices;

  public ReservedPurchaseOption() {
  }

  public String getPurchaseOption() {
    return purchaseOption;
  }

  public void setPurchaseOption(String purchaseOption) {
    this.purchaseOption = purchaseOption;
  }

  public List<ValueColumn> getValueColumns() {
    return valueColumns;
  }

  public void setValueColumns(List<ValueColumn> valueColumns) {
    this.valueColumns = valueColumns;
  }

  public Prices getPrices() {
    return prices;
  }

  public void setPrices(Prices prices) {
    this.prices = prices;
  }

}
