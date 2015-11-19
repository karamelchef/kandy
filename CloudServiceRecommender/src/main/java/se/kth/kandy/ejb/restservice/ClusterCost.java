package se.kth.kandy.ejb.restservice;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.log4j.Logger;
import se.kth.kandy.ejb.jpa.KaramelTaskStatisticsFacade;
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
 * @author Hossein
 */
@Stateless
public class ClusterCost {

  @EJB
  protected KaramelTaskStatisticsFacade karamelTaskStatisticsFacade;

  private static final Logger logger = Logger.getLogger(ClusterCost.class);
  /**
   * Simulate all available machines in an experiments
   */
  private Machines machines;

  /**
   * Represents a queue for a single machine in which tasks should be stored and then execute
   */
  private class MachineQueue {

    private Queue<Task> queue = new LinkedList<>();
    private long time = 0;

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
  private class Machines {

    private long globalDuration = 0;
    private Map<String, MachineQueue> machineQueues = new HashMap<>();

    private Machines() {
    }

    public boolean addTask(String machineId, Task task) {
      MachineQueue machineQueue;
      if (machineQueues.containsKey(machineId)) {
        machineQueue = machineQueues.get(machineId);
      } else {
        machineQueue = new MachineQueue();
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
   * @return
   */
  public TaskSubmitter getTaskSubmitter() {
    machines = new Machines();
    TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

      @Override
      public void submitTask(Task task) throws KaramelException {
        Long taskDuration = karamelTaskStatisticsFacade.averageTaskTime(task.getId());
        logger.debug(" submit : " + task.getName() + " on " + task.getMachineId() + " Type " + task.getMachine().
            getMachineType() + " Duration: " + taskDuration);
        //TODO: query the task duration from database by taskId and machineID
        task.setDuration(taskDuration);
        machines.addTask(task.getMachineId(), task);
      }

      @Override
      public void prepareToStart(Task task) throws KaramelException {
      }
    };
    return dummyTaskSubmitter;
  }

  /**
   * Time takes for the whole cluster to run.
   *
   * If there would be no info regarding a task in database whole calculation will return 0
   *
   * @param clusterYaml
   * @return long duration
   * @throws KaramelException
   */
  public long getClusterTime(String clusterYaml) throws KaramelException {
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
        return 0;
      }
      logger.debug(" succeed : " + task.getName() + " on " + task.getMachineId() + " Type " + task.getMachine().
          getMachineType() + " Duration: " + task.getDuration());
      task.succeed();
    }

    return machines.getGlobalDuration();
  }

  public BigDecimal getClusterPrice(String clusterYaml) {
    return new BigDecimal(0);
  }
}
