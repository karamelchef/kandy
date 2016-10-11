package se.kth.kandy.ejb.algorithm;

import java.io.IOException;
import java.math.BigDecimal;
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
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import se.kth.kandy.cloud.amazon.Ec2ApiWrapper;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;
import se.kth.kandy.ejb.jpa.AwsEc2InstancePriceFacade;
import se.kth.kandy.ejb.jpa.KaramelTaskStatisticsFacade;
import se.kth.kandy.json.cost.ClusterTimePrice;
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
@PrepareForTest({DagBuilder.class, ClusterDefinitionService.class, Ec2ApiWrapper.class})
@PowerMockIgnore({"org.apache.log4j.*"})
public class ClusterCostTest extends PowerMockTestCase {

  private static final Logger logger = Logger.getLogger(ClusterCostTest.class);

  @Test
  public void testEstimateAvailabilityTimeAndTrueCost() throws DagConstructionException, KaramelException, ServiceRecommanderException {

    ClusterCost clusterCost = new ClusterCost();

    KaramelTaskStatisticsFacade karamelTaskStatisticsFacadeMocked = Mockito.mock(KaramelTaskStatisticsFacade.class);
    AwsEc2InstancePriceFacade awsEc2InstancePriceFacadeMocked = Mockito.mock(AwsEc2InstancePriceFacade.class);

    // mock the task times retrieved from database
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task1", "ec2")).thenReturn(new Long(50000));
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task2", "ec2")).thenReturn(new Long(20000));
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task3", "ec2")).thenReturn(new Long(30000));
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task4", "ec2")).thenReturn(new Long(30000));
    when(karamelTaskStatisticsFacadeMocked.averageTaskTime("task5", "ec2")).thenReturn(new Long(40000));

    String machineTypeOnDemand = "ec2/eu-west-1/m3.large/ami-0307ce74/null/null";
    when(awsEc2InstancePriceFacadeMocked.getPrice("eu-west-1", "m3.large")).thenReturn(
        new BigDecimal("0.1460"));

    String machineTypeSpot = "ec2/eu-west-1/m3.large/ami-234ecc54/null/0.1";
    Ec2ApiWrapper ec2ApiWrapperMocked = Mockito.mock(Ec2ApiWrapper.class);
    when(ec2ApiWrapperMocked.getCurrentLinuxSpotPrice("m3.large", "eu-west-1")).thenReturn(new BigDecimal("0.02071667"));
    PowerMockito.mockStatic(Ec2ApiWrapper.class);
    when(Ec2ApiWrapper.getInstance()).thenReturn(ec2ApiWrapperMocked);

    clusterCost.karamelTaskStatisticsFacade = karamelTaskStatisticsFacadeMocked;
    clusterCost.awsEc2InstancePriceFacade = awsEc2InstancePriceFacadeMocked;

    PowerMockito.mockStatic(ClusterDefinitionService.class);
    when(ClusterDefinitionService.yamlToJsonObject(Mockito.anyString())).thenReturn(new JsonCluster());

    TaskSubmitter taskSubmitter = clusterCost.getTaskSubmitter();
    PowerMockito.mockStatic(DagBuilder.class);
    // test time and price for cluster running on OnDemand machine type
    Dag dagOnDemand = getDummyDag(machineTypeOnDemand, taskSubmitter);
    when(DagBuilder.getInstallationDag(any(JsonCluster.class), any(ClusterRuntime.class), any(ClusterStats.class), any(
        TaskSubmitter.class), any(Map.class))).thenReturn(dagOnDemand);
    ClusterTimePrice clusterTimePrice = clusterCost.estimateAvailabilityTimeAndTrueCost(Mockito.anyString());
    assertEquals(clusterTimePrice.getDuration(), 120000, "Calculated running time does not match"); //2 minute
    assertEquals(clusterTimePrice.getPrice(), new BigDecimal("0.2920"), "Calculated OnDemand price does not match");// in dollar

    // test time and price for cluster running on spot machine type
    Dag dagSpot = getDummyDag(machineTypeSpot, taskSubmitter);
    when(DagBuilder.getInstallationDag(any(JsonCluster.class), any(ClusterRuntime.class), any(ClusterStats.class), any(
        TaskSubmitter.class), any(Map.class))).thenReturn(dagSpot);
    clusterTimePrice = clusterCost.estimateAvailabilityTimeAndTrueCost(Mockito.anyString());
    assertEquals(clusterTimePrice.getDuration(), 120000, "Calculated running time does not match"); //2 minute
    assertEquals(clusterTimePrice.getPrice(), new BigDecimal("0.0414"), "Calculated spot sprice does not match");// in dollar

  }

  private Dag getDummyDag(String machineType, TaskSubmitter taskSubmitter) throws DagConstructionException {
    Dag dag = new Dag();
    // builds the dependancy graph
    dag.addNode("task4");
    dag.addDependency("task1", "task3");
    dag.addDependency("task2", "task5");
    //add tasks
    dag.addTask(new DummyTask("task1", "nn1", machineType, taskSubmitter));
    dag.addTask(new DummyTask("task2", "nn2", machineType, taskSubmitter));
    dag.addTask(new DummyTask("task3", "nn2", machineType, taskSubmitter));
    dag.addTask(new DummyTask("task4", "nn1", machineType, taskSubmitter));
    dag.addTask(new DummyTask("task5", "nn1", machineType, taskSubmitter));
    return dag;
  }

}

/**
 * Extend {@link Task}
 *
 * @author hossein
 */
class DummyTask extends Task {

  public DummyTask(String id, String machineId, String provider, TaskSubmitter submitter) {
    this(id, getMachineRuntime(machineId, provider), submitter);
  }

  private DummyTask(String id, MachineRuntime machineRuntime, TaskSubmitter submitter) {
    super(id, id, false, machineRuntime, new ClusterStats(), submitter);
  }

  private static MachineRuntime getMachineRuntime(String machineId, String provider) {
    MachineRuntime machineRuntime = new MachineRuntime(null);
    machineRuntime.setPublicIp(machineId);
    machineRuntime.setMachineType(provider);
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
