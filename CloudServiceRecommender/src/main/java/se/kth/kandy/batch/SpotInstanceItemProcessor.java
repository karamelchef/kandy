/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.batch;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;
import org.jclouds.aws.ec2.domain.Spot;
import se.kth.kandy.model.AwsEc2SpotInstance;

/**
 *
 * @author Hossein
 */
@Named
public class SpotInstanceItemProcessor implements ItemProcessor {

  @Override
  public List<AwsEc2SpotInstance> processItem(Object item) throws Exception {
    List<AwsEc2SpotInstance> instanceList = new ArrayList<>();
    Set<Spot> spots = (Set<Spot>) item;
    for (Spot spot : spots) {
      instanceList.add(new AwsEc2SpotInstance(spot.getRegion(), spot.getInstanceType(), spot.getProductDescription(),
          new BigDecimal(spot.getSpotPrice()), new Timestamp(spot.getTimestamp().getTime()),
          spot.getAvailabilityZone()));
    }
    return instanceList;
  }

}
