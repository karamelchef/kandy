package se.kth.kandy.ejb.awsec2instance;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.kth.kandy.ejb.AbstractFacade;
import se.kth.kandy.model.AwsEc2Instance;

/**
 *
 * @author Hossein
 */
@Stateless
public class AwsEc2InstanceFacade extends AbstractFacade<AwsEc2Instance> {

  @PersistenceContext(unitName = "ServiceRecommender-ejb_PU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public AwsEc2InstanceFacade() {
    super(AwsEc2Instance.class);
  }

}
