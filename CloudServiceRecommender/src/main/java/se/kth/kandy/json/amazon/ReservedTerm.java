package se.kth.kandy.json.amazon;

import java.util.List;

/**
 *
 * @author Hossein
 */
public class ReservedTerm {

  private String term;
  private List<ReservedPurchaseOption> onDemandHourly;
  private List<ReservedPurchaseOption> purchaseOptions;

  public ReservedTerm() {
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public List<ReservedPurchaseOption> getOnDemandHourly() {
    return onDemandHourly;
  }

  public void setOnDemandHourly(List<ReservedPurchaseOption> onDemandHourly) {
    this.onDemandHourly = onDemandHourly;
  }

  public List<ReservedPurchaseOption> getPurchaseOptions() {
    return purchaseOptions;
  }

  public void setPurchaseOptions(List<ReservedPurchaseOption> purchaseOptions) {
    this.purchaseOptions = purchaseOptions;
  }

}
