package se.kth.kandy.ejb.jpa;

import se.kth.kandy.ejb.AbstractFacade;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.kth.kandy.model.AwsEc2InstancePrice;

/**
 *
 * @author Hossein
 */
@Stateless
public class AwsEc2InstancePriceFacade extends AbstractFacade<AwsEc2InstancePrice> {

  @PersistenceContext(unitName = "ServiceRecommender-ejb_PU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public AwsEc2InstancePriceFacade() {
    super(AwsEc2InstancePrice.class);
  }

}
