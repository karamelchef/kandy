/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommender.batch;

import java.util.Date;
import java.util.List;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.ejb.EJB;
import javax.inject.Named;
import org.apache.log4j.Logger;
import se.kth.servicerecommender.ejb.awsec2instance.AwsEc2InstanceFacade;
import se.kth.servicerecommender.ejb.awsec2instance.AwsEc2InstancePriceFacade;
import se.kth.servicerecommender.ejb.notify.ServerPushFacade;
import se.kth.servicerecommender.model.AwsEc2Instance;
import se.kth.servicerecommender.model.AwsEc2InstancePrice;

/**
 *
 * @author Hossein
 */
@Named
public class AwsEc2InstanceItemWriter extends AbstractItemWriter {

  private static final Logger logger = Logger.getLogger(AwsEc2InstanceItemWriter.class);
  @EJB
  private ServerPushFacade serverPushFacade;
  @EJB
  private AwsEc2InstancePriceFacade awsEc2InstancePriceFacade;
  @EJB
  private AwsEc2InstanceFacade awsEc2InstanceFacade;

  @Override
  public void writeItems(List<Object> items) throws Exception {
    try {
      List<AwsEc2Instance> awsEc2Instances = (List<AwsEc2Instance>) items.get(0);
      for (AwsEc2Instance instance : awsEc2Instances) {
        awsEc2InstanceFacade.edit(instance);
      }
    } catch (ClassCastException e) {
      List<AwsEc2InstancePrice> awsEc2Instanceprices = (List<AwsEc2InstancePrice>) items.get(0);
      for (AwsEc2InstancePrice instancePrice : awsEc2Instanceprices) {
        awsEc2InstancePriceFacade.edit(instancePrice);
      }
    }
  }

  @Override
  public void close() {
    String log = "Aws ec2 Ondemand/Reserved instance prices persisted in Database";
    logger.info(log);
    serverPushFacade.pushLog("[ " + new Date() + " ] " + log);

  }

}
