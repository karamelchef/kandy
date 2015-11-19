package se.kth.kandy.ejb.restservice;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import static org.mockito.Matchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.kth.kandy.ejb.jpa.KaramelTaskStatisticsFacade;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.tasks.DagBuilder;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.exception.DagConstructionException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;

/**
 * Unit test for {@link ClusterCost}, no ejb container will be used.
 *
 * Mostly check the algorithm for calculating the time and price logic
 *
 * @author Hossein
 */
@PrepareForTest({DagBuilder.class, ClusterDefinitionService.class})
@PowerMockIgnore({"org.apache.log4j.*"})
public class ClusterCostTest extends PowerMockTestCase {

  private static final Logger logger = Logger.getLogger(ClusterCostTest.class);

  @Test
  public void testGetClusterTime() throws DagConstructionException, KaramelException {
    ClusterCost clusterCost = new ClusterCost();
    KaramelTaskStatisticsFacade karamelTaskStatisticsFacadeMocked = Mockito.mock(KaramelTaskStatisticsFacade.class);

    // mock the task times retrieved from database
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task1")).thenReturn(new Long(50));
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task2")).thenReturn(new Long(20));
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task3")).thenReturn(new Long(30));
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task4")).thenReturn(new Long(30));
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task5")).thenReturn(new Long(40));

    clusterCost.karamelTaskStatisticsFacade = karamelTaskStatisticsFacadeMocked;
    TaskSubmitter taskSubmitter = clusterCost.getTaskSubmitter();

    Dag dag = new Dag();

    // builds the dependancy graph
    dag.addNode("task4");
    dag.addDependency("task1", "task3");
    dag.addDependency("task2", "task5");

    //add tasks
    dag.addTask(new DummyTask("task1", "nn1", taskSubmitter));
    dag.addTask(new DummyTask("task2", "nn2", taskSubmitter));
    dag.addTask(new DummyTask("task3", "nn2", taskSubmitter));
    dag.addTask(new DummyTask("task4", "nn1", taskSubmitter));
    dag.addTask(new DummyTask("task5", "nn1", taskSubmitter));

    PowerMockito.mockStatic(DagBuilder.class);
    when(DagBuilder.getInstallationDag(any(JsonCluster.class), any(ClusterRuntime.class), any(ClusterStats.class), any(
        TaskSubmitter.class), any(Map.class))).thenReturn(dag);
    PowerMockito.mockStatic(ClusterDefinitionService.class);
    when(ClusterDefinitionService.yamlToJsonObject(Mockito.anyString())).thenReturn(new JsonCluster());
    Long runTime = clusterCost.getClusterTime(Mockito.anyString());
    logger.debug("Cluster run time : " + runTime);
    Assert.assertEquals(runTime, new Long(120), "Calculated runtime does not match");
  }

  @Test
  public void testGetClusterPrice() {
  }

}

/**
 * Extend {@link Task}
 *
 * @author hossein
 */
class DummyTask extends Task {

  public DummyTask(String id, String machineId, TaskSubmitter submitter) {
    this(id, getMachineRuntime(machineId), submitter);
  }

  private DummyTask(String id, MachineRuntime machineRuntime, TaskSubmitter submitter) {
    super(id, id, machineRuntime, new ClusterStats(), submitter);
  }

  private static MachineRuntime getMachineRuntime(String machineId) {
    MachineRuntime machineRuntime = new MachineRuntime(null);
    machineRuntime.setPublicIp(machineId);
    return machineRuntime;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String uniqueId() {
    return super.getId();
  }

  @Override
  public Set<String> dagDependencies() {
    return Collections.EMPTY_SET;
  }
}
