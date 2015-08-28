/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommander.ejb.datacontroller;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.kth.servicerecommander.model.SpotInstance;

/**
 * Enterprise java bean(Session bean) - Implements the business logic for transactions with database
 *
 * @author Hossein
 */
@Stateless
public class SpotInstanceFacade extends AbstractFacade<SpotInstance> implements SpotInstanceFacadeLocal {

  @PersistenceContext(unitName = "ServiceRecommander-ejb_PU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public SpotInstanceFacade() {
    super(SpotInstance.class);
  }

}
