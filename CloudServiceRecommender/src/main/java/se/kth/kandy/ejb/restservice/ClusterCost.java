package se.kth.kandy.ejb.restservice;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.log4j.Logger;
import se.kth.kandy.ejb.jpa.AwsEc2InstancePriceFacade;
import se.kth.kandy.ejb.jpa.AwsEc2SpotInstanceFacade;
import se.kth.kandy.ejb.jpa.KaramelTaskStatisticsFacade;
import se.kth.kandy.json.cost.ClusterTimePrice;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.converter.ChefJsonGenerator;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.mocking.MockingUtil;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.tasks.DagBuilder;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;

/**
 * Calculates expected time and price for provisioning an specified cluster in yaml format
 *
 * 1. The algorithm first simulates the running environment and machine queues involved in running the cluster.
 *
 * 2.Instead of actually running the cluster on machines it fetch the history of previously run tasks from the database
 * based on provider type EC2, GCE or BAREMETAL.
 *
 * 3. Starts the DAG and calculate the total time
 *
 * 4. For total price fetch machine prices from the database, sum the prices of machines then times previously
 * calculated duration round up to hours.
 *
 * @author Hossein
 */
@Stateless
public class ClusterCost {

  @EJB
  protected AwsEc2SpotInstanceFacade awsEc2SpotInstanceFacade;
  @EJB
  protected AwsEc2InstancePriceFacade awsEc2InstancePriceFacade;
  @EJB
  protected KaramelTaskStatisticsFacade karamelTaskStatisticsFacade;

  private static final Logger logger = Logger.getLogger(ClusterCost.class);

  /**
   * Simulates all available machines for the cluster
   */
  private Machines machines;

  /**
   * Represents a queue for a single machine in which tasks should be stored and then execute
   */
  protected class MachineQueue {

    private Queue<Task> queue = new LinkedList<>();
    private long time = 0;
    private String machineType = null;

    protected MachineQueue(String machineType) {
      this.machineType = machineType;
    }

    public boolean isEmpty() {
      return queue.isEmpty();
    }

    public boolean addTask(Task task) {
      return queue.add(task);
    }

    public Task removeTask() {
      time = 0;
      return queue.poll();
    }

    /**
     * Set queue back in time to make it synchronize with the global time with the other queues
     *
     * @param duration
     */
    public void setQueueTime(long duration) {
      this.time -= duration;
    }

    /**
     * Returns the time needed for the first task in the queue to complete
     *
     * returns max long, if queue is empty
     *
     * @return long - time needed
     */
    public long getHeadTaskDuration() {
      if (queue.peek() == null) { // if queue is empty
        return Long.MAX_VALUE;
      }
      return queue.peek().getDuration() + time;
    }
  }

  /**
   * Represents all the available machines in an experiment.
   */
  protected class Machines {

    private long globalDuration = 0;
    private Map<String, MachineQueue> machineQueues = new HashMap<>();

    protected Machines() {
    }

    public boolean addTask(String machineId, Task task) {
      MachineQueue machineQueue;
      if (machineQueues.containsKey(machineId)) {
        machineQueue = machineQueues.get(machineId);
      } else {
        machineQueue = new MachineQueue(task.getMachine().getMachineType());
      }
      machineQueue.addTask(task);
      machineQueues.put(machineId, machineQueue);
      return true;
    }

    /**
     * Find the task among all the queues head, with least time needed to finish. remove it and update queues and global
     * time
     *
     * @return removed Task from queue
     */
    public Task removeSmallestTask() {
      // Id of the machine queue, containing the shortest task in head
      String machineId = (String) machineQueues.keySet().toArray()[0];
      for (Entry<String, MachineQueue> entry : machineQueues.entrySet()) {
        if (entry.getValue().getHeadTaskDuration() < machineQueues.get(machineId).getHeadTaskDuration()) {
          machineId = entry.getKey();
        }
      }

      // Update other queues time, to make it synchronize with the queue containg the shortest task,
      // after removing the task
      for (Entry<String, MachineQueue> entry : machineQueues.entrySet()) {
        if (!entry.getKey().equalsIgnoreCase(machineId) && !entry.getValue().isEmpty()) {
          machineQueues.get(entry.getKey()).setQueueTime(machineQueues.get(machineId).getHeadTaskDuration());
        }
      }

      //move the global time forward, considering the time of the execution of the removed task
      this.globalDuration += machineQueues.get(machineId).getHeadTaskDuration();
      return machineQueues.get(machineId).removeTask();
    }

    public long getGlobalDuration() {
      return globalDuration;
    }
  }

  /**
   * Initialize the machines and implements task submitter
   *
   * @param machines
   * @return
   */
  protected TaskSubmitter getTaskSubmitter() {
    TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

      @Override
      public void submitTask(Task task) throws KaramelException {
        String provider = task.getMachine().getMachineType().split("/")[0];
        Long taskDuration = karamelTaskStatisticsFacade.averageTaskTime(task.getId(), provider);
        logger.debug(" submit : " + task.getId() + " on " + task.getMachineId() + " Type " + task.getMachine().
            getMachineType() + " Duration: " + taskDuration);
        //TODO: query the task duration from database by taskId and machineID
        task.setDuration(taskDuration);
        machines.addTask(task.getMachineId(), task);
        printMachinesStatus();
      }

      @Override
      public void prepareToStart(Task task) throws KaramelException {
      }
    };
    return dummyTaskSubmitter;
  }

  /**
   * Time and price takes for the whole cluster to run.
   *
   * If there would be no info regarding a task time in database whole calculation will return 0
   *
   * @param clusterYaml
   * @return
   * @throws KaramelException
   */
  public ClusterTimePrice getClusterCost(String clusterYaml) throws KaramelException {
    machines = new Machines();
    JsonCluster jsonCluster = ClusterDefinitionService.yamlToJsonObject(clusterYaml);
    ClusterRuntime dummyClusterRuntime = MockingUtil.dummyRuntime(jsonCluster);
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsons(jsonCluster, dummyClusterRuntime);
    ClusterStats clusterStats = new ClusterStats();
    TaskSubmitter dummyTaskSubmitter = getTaskSubmitter();

    Dag installationDag = DagBuilder.getInstallationDag(jsonCluster, dummyClusterRuntime, clusterStats,
        dummyTaskSubmitter, chefJsons);

    installationDag.validate();
    installationDag.start();

    while (!installationDag.isDone()) {
      Task task = machines.removeSmallestTask();
      if (task.getDuration() == 0) { // if there would be no time estimate for a task
        //TODO: logic can be changed
        machines.globalDuration = 0;
        break;
      }
      logger.debug(" succeed : " + task.getId() + " on " + task.getMachineId() + " Type " + task.getMachine().
          getMachineType() + " Duration: " + task.getDuration());
      task.succeed();
      printMachinesStatus();
    }

    // Calculate cost of all machines regarding the total time they are running
    BigDecimal totalCost = BigDecimal.ZERO;
    for (MachineQueue machineQueue : machines.machineQueues.values()) {
      String[] machineType = machineQueue.machineType.split("/");
      if (machineType[0].equalsIgnoreCase("ec2")) {
        // run time is rounded up in hours, regarding amazon ec2 pricing policy
        BigDecimal runTimeHours = new BigDecimal((double) machines.getGlobalDuration() / 3600000).setScale(0,
            RoundingMode.CEILING);
        BigDecimal price = BigDecimal.ZERO;
        //TODO: os and purchase option are assumed default, need to specify them from ami
        if (machineType[5].equalsIgnoreCase("null")) { // OnDemand or Reserved Instances
          price = awsEc2InstancePriceFacade.getPrice(machineType[1], machineType[2], "Linux", "ODHourly");
        } else { // Spot instances
          price = awsEc2SpotInstanceFacade.getAveragePrice(machineType[1], machineType[2], "Linux/UNIX");
        }
        totalCost = totalCost.add(price.multiply(runTimeHours));

      } else {
        //TODO: calculate price for Baremetal and GCE, for now it would be just zero
        totalCost = BigDecimal.ZERO;
        break;
      }
    }
    ClusterTimePrice clusterTimePrice = new ClusterTimePrice(machines.getGlobalDuration(), totalCost.setScale(4,
        RoundingMode.HALF_UP));
    logger.debug("Cluster run time (ms): " + clusterTimePrice.getDuration());
    logger.debug("Cluster price ($/hour): " + clusterTimePrice.getPrice());
    return clusterTimePrice;
  }

  /**
   * Prints out machine queue status
   *
   * Just used for reporting purposes.
   */
  private void printMachinesStatus() {
    logger.debug("Global time: " + machines.getGlobalDuration());
    for (Entry<String, MachineQueue> entry : machines.machineQueues.entrySet()) {
      String status = "Machine: " + entry.getKey() + " Time: " + entry.getValue().time + " -> ";
      Iterator<Task> iterator = entry.getValue().queue.iterator();
      while (iterator.hasNext()) {
        Task task = iterator.next();
        status = status.concat("| " + task.getName() + " (" + task.getDuration() + ") ");
      }
      logger.debug(status);
    }
  }
}
