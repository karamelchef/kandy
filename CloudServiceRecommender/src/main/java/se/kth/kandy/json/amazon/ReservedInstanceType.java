package se.kth.kandy.json.amazon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 *
 * @author Hossein
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservedInstanceType {

  private String type;
  private List<ReservedTerm> terms;

  public ReservedInstanceType() {
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<ReservedTerm> getTerms() {
    return terms;
  }

  public void setTerms(List<ReservedTerm> terms) {
    this.terms = terms;
  }

}
