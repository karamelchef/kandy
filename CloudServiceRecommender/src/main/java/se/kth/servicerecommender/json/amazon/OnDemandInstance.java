package se.kth.servicerecommender.json.amazon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author Hossein
 */
public class OnDemandInstance {

  private String size;
  private String vCPU;
  @JsonProperty("ECU")
  private String ECU;
  private String memoryGiB;
  private String storageGB;
  private List<ValueColumn> valueColumns;

  public OnDemandInstance() {
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getvCPU() {
    return vCPU;
  }

  public void setvCPU(String vCPU) {
    this.vCPU = vCPU;
  }

  public String getECU() {
    return ECU;
  }

  public void setECU(String ECU) {
    this.ECU = ECU;
  }

  public String getMemoryGiB() {
    return memoryGiB;
  }

  public void setMemoryGiB(String memoryGiB) {
    this.memoryGiB = memoryGiB;
  }

  public String getStorageGB() {
    return storageGB;
  }

  public void setStorageGB(String storageGB) {
    this.storageGB = storageGB;
  }

  public List<ValueColumn> getValueColumns() {
    return valueColumns;
  }

  public void setValueColumns(List<ValueColumn> valueColumns) {
    this.valueColumns = valueColumns;
  }

}
