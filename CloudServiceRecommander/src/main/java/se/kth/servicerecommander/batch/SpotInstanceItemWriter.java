/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommander.batch;

import java.util.List;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.ejb.EJB;
import javax.inject.Named;
import se.kth.servicerecommander.ejb.datacontroller.SpotInstanceFacadeLocal;
import se.kth.servicerecommander.model.SpotInstance;

/**
 *
 * @author Hossein
 */
@Named
public class SpotInstanceItemWriter extends AbstractItemWriter {

  @EJB
  private SpotInstanceFacadeLocal spotInstanceFacade;

  @Override
  public void writeItems(List<Object> items) throws Exception {
    spotInstanceFacade.create((SpotInstance) items.get(0));
  }

}
