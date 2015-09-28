/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.cloud.common.exception;

/**
 *
 * @author kamal
 */
public class InvalidEc2CredentialsException extends ServiceRecommanderException {

  public InvalidEc2CredentialsException() {
  }

  public InvalidEc2CredentialsException(String message) {
    super(message);
  }

  public InvalidEc2CredentialsException(Throwable exception) {
    super(exception);
  }

  public InvalidEc2CredentialsException(String message, Throwable exception) {
    super(message, exception);
  }

}
