/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.ejb.jpa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import se.kth.kandy.ejb.AbstractFacade;
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

  /**
   * Get average price for an amazon spot instace
   *
   * @param region
   * @param type
   * @param os
   * @return
   */
  public BigDecimal getAveragePrice(String region, String type, String os) {
    Query query = getEntityManager().createNamedQuery("AwsEc2SpotInstance.priceList");
    query.setParameter("instanceType", type);
    query.setParameter("productDescription", os);
    query.setParameter("region", region);
    query.setMaxResults(15);
    if (query.getResultList() == null) {
      return BigDecimal.ZERO;
    }
    List<BigDecimal> priceList = (List<BigDecimal>) query.getResultList();
    BigDecimal total = BigDecimal.ZERO;
    for (BigDecimal price : priceList) {
      total = total.add(price);
    }
    return total.divide(new BigDecimal(priceList.size()), 8, RoundingMode.HALF_UP);
  }
}
