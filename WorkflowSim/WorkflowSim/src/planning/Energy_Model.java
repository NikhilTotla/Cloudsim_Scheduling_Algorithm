package planning;

/**
 * SOURCE CODE MODIFIED FROM WORKFLOWSIM HEFT
 */
/**
 * Copyright 2012-2013 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.cloudbus.cloudsim.Log;

import org.workflowsim.Task;


import utils.TaskRank;
import vm.CustomVM;


/**
 * The HEFT planning algorithm.
 *
 * @author Pedro Paulo Vezzá Campos
 * @date Oct 12, 2013
 */
public class Energy_Model extends BasePlanning {

    public Map<Task, Double> rank;

    public Energy_Model() {
        rank = new HashMap<>();
    }

    /**
     * The main function
     */
    @Override
    public void run() {
        Log.printLine("Energy planner running with " + getTaskList().size()
                + " tasks and " + getVmList().size() + "vms.");
        averageBandwidth = calculateAverageBandwidth();
        System.out.println("The average Bamdwidth is = "+ averageBandwidth);
//        resetSchedules();
        calculateComputationCosts();
        calculateTransferCosts();
//         Prioritization phase
        calculateRanks();

        // Selection phase
//        allocateTasks();
    }

    /**
     * Invokes calculateRank for each task to be scheduled
     */
    public void MergeTasks() {
    
    	 Queue<Integer> queue = new LinkedList<>();
    	 Map<Integer, Task>findId = new HashMap<>();
    	 for(Task task : getTaskList())
    	 {
    		 findId.put(task.getCloudletId(), task);
    		 if(task.getParentList().size() == 0)
    			 queue.offer(task.getCloudletId());
    	 }
    	 
    	 Boolean[] isvisit = new Boolean[getTaskList().size()];
    	 while(!queue.isEmpty())
    	 {
    		 int p1 = queue.peek();
    		 queue.poll();
//    		 getCloudletById(getTask
    		 isvisit[p1] = true;
    		 if(findId.get(p1).getChildList().size() == 1)
    		 {
    			 Task parent = findId.get(p1);
    			 Task child = findId.get(p1).getChildList().getFirst();
    			 if(child.getParentList().size() == 1)
    			 {
					 List<Task> newParentList =  new LinkedList<>();

    				 for(Task task : parent.getParentList())
    				 {
    					 newParentList.add(task);
    					 List<Task> newchildList =  new LinkedList<>();
    					 for(Task task1 : task.getChildList())
    					 {
    						 if(task1 != parent) newchildList.add(task1);
    						 else newchildList.add(child);
    					 }
    					 task.setChildList(newchildList);
    				 }
    				 child.setParentList(newParentList);
    				 
    			 }

    		 }
    		 for(Task task : findId.get(p1).getChildList())
    		 {
    			 if(isvisit[task.getCloudletId()] == false) queue.offer(task.getCloudletId());
    		 }

    		
    	 }
    }
    
    public void calculateRanks() {
        for (Task task : getTaskList()) {
//            calculateRank(task);
        	System.out.println(task.getCloudletId());
        }
//        System.out.println("Rank");
//        System.out.println(rank.size());
//       rank.forEach((key, value) -> System.out.println(key + " 	: 	" + key.getCloudletId()+ "	 : 	"+ key.getCloudletTotalLength() + "	 : 	"+ value));
    }
    

    /**
     * Populates rank.get(task) with the rank of task as defined in the HEFT
     * paper.
     *
     * @param task The task have the rank calculates
     * @return The rank
     */
    public double calculateRank(Task task) {
        if (rank.containsKey(task)) {
            return rank.get(task);
        }

        double averageComputationCost = 0.0;

        for (Double cost : computationCosts.get(task).values()) {
            averageComputationCost += cost;
        }

        averageComputationCost /= computationCosts.get(task).size();


        double max = 0.0;
        for (Task child : task.getChildList()) {
            double childCost = transferCosts.get(task).get(child)
                    + calculateRank(child);
            max = Math.max(max, childCost);
        }
        rank.put(task, averageComputationCost + max);

        return rank.get(task);
    }

    /**
     * Allocates all tasks to be scheduled in non-ascending order of schedule.
     */
    public void allocateTasks() {
        List<TaskRank> taskRank = new ArrayList<>();
        for (Task task : rank.keySet()) {
            taskRank.add(new TaskRank(task, rank.get(task)));
        }

        // Sorting in non-ascending order of rank
        Collections.sort(taskRank);
        for (TaskRank rank : taskRank) {
            allocateTask(rank.task);
        }
     
    }

    /**
     * Schedules the task given in one of the VMs minimizing the earliest finish
     * time
     *
     * @param task The task to be scheduled
     * @pre All parent tasks are already scheduled
     */
    public void allocateTask(Task task) {
        CustomVM chosenVM = null;
        double earliestFinishTime = Double.MAX_VALUE;
        double bestReadyTime = 0.0;
        double finishTime;

        for (CustomVM vm : getVmList()) {
            double minReadyTime = 0.0;

            for (Task parent : task.getParentList()) {
                double readyTime = earliestFinishTimes.get(parent);
                if (parent.getVmId() != vm.getId()) {
                    readyTime += transferCosts.get(parent).get(task);
                }
                minReadyTime = Math.max(minReadyTime, readyTime);
            }

            finishTime = findFinishTime(task, vm, minReadyTime, false);

            if (finishTime < earliestFinishTime) {
                bestReadyTime = minReadyTime;
                earliestFinishTime = finishTime;
                chosenVM = vm;
            }
        }

        findFinishTime(task, chosenVM, bestReadyTime, true);
        earliestFinishTimes.put(task, earliestFinishTime);

        task.setVmId(chosenVM.getId());
    }


}
