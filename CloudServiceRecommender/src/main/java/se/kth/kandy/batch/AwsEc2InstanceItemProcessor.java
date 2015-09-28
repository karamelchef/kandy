/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.ArrayList;
import java.util.List;
import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;
import se.kth.kandy.json.amazon.AwsEc2Instances;
import se.kth.kandy.json.amazon.OnDemandInstance;
import se.kth.kandy.json.amazon.OnDemandInstanceType;
import se.kth.kandy.json.amazon.Region;
import se.kth.kandy.json.amazon.ReservedInstanceType;
import se.kth.kandy.json.amazon.ReservedPurchaseOption;
import se.kth.kandy.json.amazon.ReservedTerm;
import se.kth.kandy.model.AwsEc2Instance;
import se.kth.kandy.model.AwsEc2InstancePrice;
import se.kth.kandy.model.AwsEc2InstancePriceID;

/**
 *
 * @author Hossein
 */
@Named
public class AwsEc2InstanceItemProcessor implements ItemProcessor {

  @Override
  public Object processItem(Object item) throws Exception {
    AwsEc2InstancesJsonPerOS awsEc2InstancesJsonPerOS = (AwsEc2InstancesJsonPerOS) item;
    ObjectMapper mapper = new ObjectMapper();

    try {

      AwsEc2Instances awsEc2Instances = mapper.readValue(awsEc2InstancesJsonPerOS.getJson(),
          new TypeReference<AwsEc2Instances<OnDemandInstanceType>>() {
          });
      List<AwsEc2Instance> instancesList = new ArrayList<>();
      Region region = (Region) awsEc2Instances.getConfig().getRegions().get(0);
      for (int i = 0; i < region.getInstanceTypes().size(); i++) {
        OnDemandInstanceType onDemandInstanceType = (OnDemandInstanceType) region.getInstanceTypes().get(i);
        for (int j = 0; j < onDemandInstanceType.getSizes().size(); j++) {
          OnDemandInstance onDemandInstance = onDemandInstanceType.getSizes().get(j);
          instancesList.add(
              new AwsEc2Instance(onDemandInstance.getSize(), Integer.valueOf(onDemandInstance.getvCPU()),
                  onDemandInstance.getECU(),
                  Float.valueOf(onDemandInstance.getMemoryGiB()), onDemandInstance.getStorageGB()));
        }
      }
      return instancesList;

    } catch (UnrecognizedPropertyException e) {

      AwsEc2Instances awsEc2Instances = mapper.readValue(awsEc2InstancesJsonPerOS.getJson(),
          new TypeReference<AwsEc2Instances<ReservedInstanceType>>() {
          });

      List<AwsEc2InstancePrice> instancesList = new ArrayList<>();

      for (int i = 0; i < awsEc2Instances.getConfig().getRegions().size(); i++) {
        Region region = (Region) awsEc2Instances.getConfig().getRegions().get(i);
        for (int j = 0; j < region.getInstanceTypes().size(); j++) {
          ReservedInstanceType reservedInstanceType = (ReservedInstanceType) region.getInstanceTypes().get(j);
          for (int k = 0; k < reservedInstanceType.getTerms().size(); k++) {
            ReservedTerm reservedTerm = reservedInstanceType.getTerms().get(k);

            AwsEc2InstancePrice awsEc2InstancePrice = new AwsEc2InstancePrice(new AwsEc2InstancePriceID(
                region.getRegion(), awsEc2InstancesJsonPerOS.getOsType(), reservedTerm.getOnDemandHourly().get(0).
                getPurchaseOption()), Float.valueOf(reservedTerm.getOnDemandHourly().get(0).getPrices().getUSD()),
                new AwsEc2Instance(reservedInstanceType.getType()));

            if (!instancesList.contains(awsEc2InstancePrice)) {
              instancesList.add(awsEc2InstancePrice);
            }

            for (int l = 0; l < reservedTerm.getPurchaseOptions().size(); l++) {
              ReservedPurchaseOption reservedPurchaseOption = reservedTerm.getPurchaseOptions().get(l);

              awsEc2InstancePrice = new AwsEc2InstancePrice(new AwsEc2InstancePriceID(region.getRegion(),
                  awsEc2InstancesJsonPerOS.getOsType(), reservedTerm.getTerm() + "_" + reservedPurchaseOption.
                  getPurchaseOption()), 0, new AwsEc2Instance(reservedInstanceType.getType()));

              for (int m = 0; m < reservedPurchaseOption.getValueColumns().size(); m++) {
                String purchaseType = reservedPurchaseOption.getValueColumns().get(m).getName();
                if (purchaseType.equalsIgnoreCase("effectiveHourly")) {
                  awsEc2InstancePrice.setPrice(Float.valueOf(
                      reservedPurchaseOption.getValueColumns().get(m).getPrices().getUSD()));
                }
              }

              instancesList.add(awsEc2InstancePrice);
            }

          }
        }
      }

      return instancesList;
    }
  }

}
