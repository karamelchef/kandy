package se.kth.kandy.ejb.restservice;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import se.kth.kandy.ejb.algorithm.InstanceFilter;

/**
 *
 * @author Hossein
 */
@XmlRootElement
public class InstanceSpecification implements Serializable {

  public long availabilityTime;
  public float reliabilityLowerBound;
  public InstanceFilter.ECU minECU;
  public float minMemoryGB;
  public InstanceFilter.STORAGEGB minStorage;

  public InstanceSpecification() {
  }

  public long getAvailabilityTime() {
    return availabilityTime;
  }

  public float getReliabilityLowerBound() {
    return reliabilityLowerBound;
  }

  public InstanceFilter.ECU getMinECU() {
    return minECU;
  }

  public float getMinMemoryGB() {
    return minMemoryGB;
  }

  public InstanceFilter.STORAGEGB getMinStorage() {
    return minStorage;
  }
}
