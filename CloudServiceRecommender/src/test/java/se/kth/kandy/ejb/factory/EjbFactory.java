package se.kth.kandy.ejb.factory;

import java.util.Properties;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;
import se.kth.kandy.ejb.restservice.ClusterCostFacadeREST;

/**
 * Singleton design pattern. Factory for initializing session beans for integration tests
 *
 * Uses OpenEjb and tomee. Database name user and pass should be provided for the JPA to be able to connect.
 *
 * @author Hossein
 */
public class EjbFactory {

  private static final Logger logger = Logger.getLogger(EjbFactory.class);
  private static EJBContainer container;
  private static EjbFactory ejbFactory = new EjbFactory();

  /**
   * Specifies database Url, User and Pass. A database with this same name should already be exist in the mysql
   */
  private final static String databaseUrl = "jdbc:mysql://130.237.238.190:3306/servicerecommender";
  private final static String databaseUser = "kthfs";
  private final static String databasePass = "kthfs";

  private EjbFactory() {
    if (container == null) {
      Properties properties = new Properties();
      properties.put("jdbc/bankWorkTestResource", "new://Resource?type=DataSource");
      properties.put("jdbc/bankWorkTestResource.JdbcDriver", "com.mysql.jdbc.Driver");
      properties.put("jdbc/bankWorkTestResource.JdbcUrl", databaseUrl);
      properties.put("jdbc/bankWorkTestResource.UserName", databaseUser);
      properties.put("jdbc/bankWorkTestResource.Password", databasePass);

      container = EJBContainer.createEJBContainer(properties);
    }
  }

  public static EjbFactory getInstance() {
    return ejbFactory;
  }

  public void closeFactory() {
    container.close();
  }

  public ClusterCostFacadeREST getClusterCostFacadeREST() {
    try {
      return (ClusterCostFacadeREST) container.getContext().lookup(
          "java:global/CloudServiceRecommender/ClusterCostFacadeREST");
    } catch (NamingException ex) {
      logger.error("Could not resolve session bean", ex);
    }
    return null;
  }

  public AwsEc2SpotInstanceFacade getAwsEc2SpotInstanceFacade() {
    try {
      return (AwsEc2SpotInstanceFacade) container.getContext().lookup(
          "java:global/CloudServiceRecommender/AwsEc2SpotInstanceFacade");
    } catch (NamingException ex) {
      logger.error("Could not resolve session bean", ex);
    }
    return null;
  }

}
