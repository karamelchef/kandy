/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommender.batch;

import java.util.List;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.ejb.EJB;
import javax.inject.Named;
import se.kth.servicerecommender.ejb.awsec2spotinstance.AwsEc2SpotInstanceFacadeLocal;
import se.kth.servicerecommender.model.AwsEc2SpotInstance;

/**
 *
 * @author Hossein
 */
@Named
public class SpotInstanceItemWriter extends AbstractItemWriter {

  @EJB
  private AwsEc2SpotInstanceFacadeLocal spotInstanceFacade;

  @Override
  public void writeItems(List<Object> items) throws Exception {
    spotInstanceFacade.create((AwsEc2SpotInstance) items.get(0));
  }

}
