package se.kth.kandy.ejb.jpa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
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

  /**
   * Query the database for the last date, spot instance prices had been fetched until that, if there is no such a date
   * will return min long value
   *
   * @return long
   */
  public long getlastSamplingDate() {
    Query query = getEntityManager().createNamedQuery("AwsEc2SpotInstance.lastSamplingDate");
    Date timeStamp = (Date) query.getSingleResult();
    if (timeStamp != null) {
      return timeStamp.getTime();
    } else {
      return Long.MIN_VALUE;
    }
  }

  /**
   * Query list of spot instances with specified instanceType and availability zone
   *
   * @param instanceType
   * @param availabilityZone
   * @return list of instances sorted ascending
   */
  public List<AwsEc2SpotInstance> getSpotInstanceList(String instanceType, String availabilityZone) {
    Query query = getEntityManager().createNamedQuery("AwsEc2SpotInstance.instancePriceList", AwsEc2SpotInstance.class);
    query.setParameter("instanceType", instanceType);
    query.setParameter("productDescription", "Linux/UNIX");
    query.setParameter("availabilityZone", availabilityZone);
    return (List<AwsEc2SpotInstance>) query.getResultList();
  }

  /**
   *
   * @param instanceType
   * @return
   */
  public List<String> getAvailabilityZones(String instanceType) {
    Query query = getEntityManager().createNamedQuery("AwsEc2SpotInstance.availabilityZone", AwsEc2SpotInstance.class);
    query.setParameter("instanceType", instanceType);
    query.setParameter("productDescription", "Linux/UNIX");
    return (List<String>) query.getResultList();
  }
}
