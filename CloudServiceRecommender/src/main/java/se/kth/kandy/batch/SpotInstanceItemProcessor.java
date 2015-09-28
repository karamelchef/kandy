/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.batch;

import java.sql.Timestamp;
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
  public AwsEc2SpotInstance processItem(Object item) throws Exception {
    Spot spot = (Spot) item;
    return new AwsEc2SpotInstance(spot.getRegion(), spot.getInstanceType(), spot.getProductDescription(), spot.
        getSpotPrice(), new Timestamp(spot.getTimestamp().getTime()), spot.getAvailabilityZone());
  }

}
