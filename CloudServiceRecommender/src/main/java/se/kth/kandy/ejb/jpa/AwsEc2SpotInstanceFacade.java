/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.ejb.jpa;

import se.kth.kandy.ejb.AbstractFacade;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.kth.kandy.model.AwsEc2SpotInstance;

/**
 * Enterprise java bean(Session bean) - Implements the business logic for transactions with database
 *
 * @author Hossein
 */
@Stateless
public class AwsEc2SpotInstanceFacade extends AbstractFacade<AwsEc2SpotInstance> {

  @PersistenceContext(unitName = "ServiceRecommender-ejb_PU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public AwsEc2SpotInstanceFacade() {
    super(AwsEc2SpotInstance.class);
  }

}
