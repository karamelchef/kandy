/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.ejb.notify;

import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

/**
 *
 * @author Hossein
 */
@PushEndpoint("/notify")
public class NotifyResource {

  @OnMessage(encoders = {JSONEncoder.class})
  public String onMessage(String message) {
    return message;
  }
}
