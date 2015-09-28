/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.ejb.notify;

import javax.ejb.Stateless;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

/**
 * Enterprise session bean. used to broadcast logs back to client browser
 *
 * @author Hossein
 */
@Stateless
public class ServerPushFacade {

  private final static String CHANNEL = "/notify";

  public void pushLog(String log) {
    EventBus eventBus = EventBusFactory.getDefault().eventBus();
    eventBus.publish(CHANNEL, log);
  }

}
