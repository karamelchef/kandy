/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.servicerecommander.cloud.common.exception;

/**
 *
 * @author Hossein
 */
public class ServiceRecommanderException extends Exception {

  public ServiceRecommanderException() {
  }

  public ServiceRecommanderException(String message) {
    super(message);
  }
  
  public ServiceRecommanderException(Throwable exception) {
    super(exception);
  }
  
  public ServiceRecommanderException(String message, Throwable exception) {
    super(message, exception);
  } 
}
