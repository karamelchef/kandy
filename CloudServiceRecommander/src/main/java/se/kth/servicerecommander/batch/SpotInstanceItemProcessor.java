/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommander.batch;

import java.sql.Timestamp;
import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;
import org.jclouds.aws.ec2.domain.Spot;
import se.kth.servicerecommander.model.SpotInstance;

/**
 *
 * @author Hossein
 */
@Named
public class SpotInstanceItemProcessor implements ItemProcessor {

  @Override
  public SpotInstance processItem(Object item) throws Exception {
    Spot spot = (Spot) item;
    return new SpotInstance(spot.getRegion(), spot.getInstanceType(), spot.getProductDescription(), spot.getSpotPrice(),
        new Timestamp(spot.getTimestamp().getTime()), spot.getAvailabilityZone());
  }

}
