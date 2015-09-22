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
import se.kth.servicerecommender.ejb.awsec2instance.AwsEc2SpotInstanceFacade;
import se.kth.servicerecommender.ejb.notify.ServerPushFacade;
import se.kth.servicerecommender.model.AwsEc2SpotInstance;

/**
 *
 * @author Hossein
 */
@Named
public class SpotInstanceItemWriter extends AbstractItemWriter {

  private static final Logger logger = Logger.getLogger(SpotInstanceItemWriter.class);
  @EJB
  private ServerPushFacade serverPushFacade;
  @EJB
  private AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;

  @Override
  public void writeItems(List<Object> items) throws Exception {
    awsEc2SpotInstanceFacade.create((AwsEc2SpotInstance) items.get(0));
  }

  @Override
  public void close() {
    String log = "Aws ec2 spot instance prices persisted in Database";
    logger.info(log);
    serverPushFacade.pushLog("[ " + new Date() + " ] " + log);
  }

}
