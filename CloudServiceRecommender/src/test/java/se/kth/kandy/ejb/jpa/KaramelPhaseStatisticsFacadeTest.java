package se.kth.kandy.ejb.jpa;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.kth.kandy.ejb.factory.EjbFactory;
import se.kth.kandy.model.KaramelStatistics;

/**
 *
 * @author hossein
 */
public class KaramelPhaseStatisticsFacadeTest {

  private KaramelPhaseStatisticsFacade karamelPhaseStatisticsFacade = null;

  @BeforeClass
  public void setUpClass() {
    karamelPhaseStatisticsFacade = EjbFactory.getInstance().getKaramelPhaseStatisticsFacade();
  }

  @Test
  public void testMaxID() {
    karamelPhaseStatisticsFacade.maxID(new KaramelStatistics(182661L));
  }

}
