package se.kth.kandy.ejb.jpa;

import java.math.BigDecimal;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import se.kth.kandy.ejb.AbstractFacade;
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

  /**
   * Return price of the specified instance type from database.
   *
   * @param region
   * @param type
   * @param os
   * @param purchaseOption
   * @return
   */
  public BigDecimal getPrice(String region, String type) {
    Query query = getEntityManager().createNamedQuery("AwsEc2InstancePrice.price");
    query.setParameter("instanceType", type);
    query.setParameter("instanceOs", "Linux");
    query.setParameter("purchaseOption", "ODHourly");
    query.setParameter("region", region);
    if (query.getSingleResult() == null) {
      return BigDecimal.ZERO;
    }
    return (BigDecimal) query.getSingleResult();
  }

  /**
   *
   * @param instanceType
   * @return
   */
  public List<String> getRegions(String instanceType) {
    Query query = getEntityManager().createNamedQuery("AwsEc2InstancePrice.region");
    query.setParameter("instanceType", instanceType);
    query.setParameter("instanceOs", "Linux");
    query.setParameter("purchaseOption", "ODHourly");
    return (List<String>) query.getResultList();
  }
}
