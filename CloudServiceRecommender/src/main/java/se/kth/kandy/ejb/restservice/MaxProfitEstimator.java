package se.kth.kandy.ejb.restservice;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.log4j.Logger;
import se.kth.kandy.ejb.jpa.AwsEc2InstanceFacade;
import se.kth.kandy.model.AwsEc2Instance;

/**
 *
 * @author Hossein
 */
@Stateless
public class MaxProfitEstimator {

  private static final Logger logger = Logger.getLogger(MaxProfitEstimator.class);

  @EJB
  private AwsEc2InstanceFacade awsEc2InstanceFacade;

  /**
   * AWS ec2 instance types Ec2 compute unit
   */
  public enum ECU {

    ALL(0), VARIABLE(0), FIXED3(3), FIXED6p5(6.5f), FIXED7(7), FIXED8(8), FIXED13(13), FIXED14(14), FIXED16(16),
    FIXED26(26), FIXED27(27), FIXED28(28), FIXED31(31), FIXED52(52), FIXED53(53), FIXED53p5(53.5f), FIXED55(55),
    FIXED56(56), FIXED62(62), FIXED104(104), FIXED108(108), FIXED116(116), FIXED124p5(124.5f), FIXED132(132);

    private float ecu;

    private ECU(float ecu) {
      this.ecu = ecu;
    }

    public float getValue() {
      return ecu;
    }
  }

  /**
   * Attached storage to EC2 instances
   */
  public enum STORAGEGB {

    ALL(0), EBSONLY(0), SSD4(4), SSD32(32), SSD60(60), SSD80(80), SSD160(160), SSD240(240), SSD320(320), SSD360(360),
    SSD640(640), SSD800(800), SSD1600(1600), SSD2400(2400), SSD6400(6400), HDD6000(6000), HDD12000(12000), HDD24000(
        24000), HDD48000(48000);

    private int storage;

    private STORAGEGB(int storage) {
      this.storage = storage;
    }

    public int getValue() {
      return storage;
    }
  }

  /**
   * Return list of ec2 instances with specified minimum requirement.
   *
   * @param minECU - It can be variable for T2 instances or fixed number. If user choose ALL, then no filtering will
   * happen for this part
   * @param minMemoryGB - represents minimum RAM
   * @param minStorage - It can be EBSONLY or HDD/SSD with a number. If user choose ALL, then no filtering will happen
   * for this part
   *
   * @return - List of Ec2 instance types with minimum requirement
   */
  public List<String> filterEc2InstanceTypes(ECU minECU, float minMemoryGB, STORAGEGB minStorage) {
    List<AwsEc2Instance> awsEc2Instances = awsEc2InstanceFacade.findAll();
    List<String> filteredInstances = new ArrayList<>();
    for (AwsEc2Instance instance : awsEc2Instances) {

      // reagrding the specified minEcu, divide the search scope to two different categories variable and others(fixed)
      if (minECU != ECU.ALL) {
        if (instance.getECU().equalsIgnoreCase("variable")) {
          if (minECU != ECU.VARIABLE) {
            continue;
          }
        } else {
          if ((minECU == ECU.VARIABLE) || (Float.valueOf(instance.getECU()) < minECU.getValue())) {
            continue;
          }
        }
      }

      if (instance.getMemoryGiB() < minMemoryGB) {
        continue;
      }

      //divide the search scope to two different categories EBSONLY and HDD/SSD
      if (minStorage != STORAGEGB.ALL) {
        if (instance.getStorageGB().equalsIgnoreCase("ebsonly")) {
          if (minStorage != STORAGEGB.EBSONLY) {
            continue;
          }
        } else {
          StringTokenizer st = new StringTokenizer(instance.getStorageGB(), " ");
          int instanceStorage = Integer.valueOf(st.nextToken());
          if (st.nextToken().equalsIgnoreCase("x")) {
            instanceStorage *= Integer.valueOf(st.nextToken());
          }
          if ((minStorage == STORAGEGB.EBSONLY) || instanceStorage < minStorage.getValue()) {
            continue;
          }
        }
      }

      filteredInstances.add(instance.getName());
    }
    logger.debug(
        "Ec2 instance types with minEcu: " + minECU.toString() + " minMemory: " + minMemoryGB + " minStorage: "
        + minStorage.toString() + " -> " + filteredInstances.toString());
    return filteredInstances;
  }

}
