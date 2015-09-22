package se.kth.servicerecommender.model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author Hossein
 */
@Entity
public class AwsEc2Instance implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  private String name;
  private int vCPU;
  private String ECU;
  private float memoryGiB;
  private String storageGB;

  public AwsEc2Instance(String name, int vCPU, String ECU, float memoryGiB, String storageGB) {
    this.name = name;
    this.vCPU = vCPU;
    this.ECU = ECU;
    this.memoryGiB = memoryGiB;
    this.storageGB = storageGB;
  }

  public AwsEc2Instance(String name) {
    this.name = name;
  }

  public AwsEc2Instance() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getvCPU() {
    return vCPU;
  }

  public void setvCPU(int vCPU) {
    this.vCPU = vCPU;
  }

  public String getECU() {
    return ECU;
  }

  public void setECU(String ECU) {
    this.ECU = ECU;
  }

  public float getMemoryGiB() {
    return memoryGiB;
  }

  public void setMemoryGiB(float memoryGiB) {
    this.memoryGiB = memoryGiB;
  }

  public String getStorageGB() {
    return storageGB;
  }

  public void setStorageGB(String storageGB) {
    this.storageGB = storageGB;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 71 * hash + Objects.hashCode(this.name);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AwsEc2Instance other = (AwsEc2Instance) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "AwsEc2Instance{" + "name=" + name + '}';
  }

}
