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
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.Log;
import org.workflowsim.FileItem;
import org.workflowsim.Task;
import org.workflowsim.utils.Parameters;
import utils.Event;
import utils.TaskRank;
import vm.CustomVM;



public class CPOP extends BasePlanning {

   
	public Map<Task, Double> rank;
    public Map<Task, Double> u_rank;
    public Map<Task, Double> d_rank;
    public double CT_Rank= 0.0;
    public CPOP() {
        u_rank = new HashMap<>();
        d_rank = new HashMap<>();
        rank = new HashMap<>();
    }

    /**
     * The main function
     */
    @Override
    public void run() {
        
        averageBandwidth = calculateAverageBandwidth();
        Log.printLine("CPOP planner running with " + getTaskList().size()
                + " tasks and " + getVmList().size() + "vms and average bandwidth."+ averageBandwidth );
        resetSchedules();
        calculateComputationCosts();
        calculateTransferCosts();
        // Prioritization phase
        calculateRanks();
        calculateCT_Rank();
        // Selection phase
        allocateTasks();
    }

    /**
     * Invokes calculateRank for each task to be scheduled
     */
    public void calculateRanks() {
        for (Task task : getTaskList()) {
     double     Ru =   calculateUpwardRank(task);
     double     Rd=      calculateDownwardRank(task);
            double Sum = Ru+Rd;
            rank.put(task, Sum);
        }
        
        System.out.println(" Upward Rank");
        System.out.println(u_rank.size());
       u_rank.forEach((key, value) -> System.out.println(key + " 	: 	" + key.getCloudletId()+ "	 : 	"+ key.getCloudletTotalLength() + "	 : 	"+ value));
       System.out.println(" Downward Rank");
       System.out.println(d_rank.size());
      d_rank.forEach((key, value) -> System.out.println(key + " 	: 	" + key.getCloudletId()+ "	 : 	"+ key.getCloudletTotalLength() + "	 : 	"+ value));
      System.out.println(" Sum Rank");
      System.out.println(rank.size());
     rank.forEach((key, value) -> System.out.println(key + " 	: 	" + key.getCloudletId()+ "	 : 	"+ key.getCloudletTotalLength() + "	 : 	"+ value));
    }

    /**
     * Populates rank.get(task) with the rank of task as defined in the HEFT
     * paper.
     *
     * @param task The task have the rank calculates
     * @return The rank
     */
    
    private double calculateDownwardRank(Task task) {
		  
    	if (d_rank.containsKey(task)) {
                return d_rank.get(task);
           }

           double max = 0.0;
           for (Task parent : task.getParentList()) {
        	  
           	double averageComputationCost = 0.0;

               for (Double cost : computationCosts.get(parent).values()) {
                   averageComputationCost += cost;
                   
               }

               averageComputationCost /= computationCosts.get(task).size();
               if(task.getParentList() != null) {
               double parentCost = transferCosts.get(parent).get(task)
                       + calculateDownwardRank(parent)+ averageComputationCost ;
               max = Math.max(max, parentCost);}
               else max=0;
              
           }
           d_rank.put(task, max);
           return d_rank.get(task);
    		
    	}
    	
    
    
    public double calculateUpwardRank(Task task) {
        if (u_rank.containsKey(task)) {
            return u_rank.get(task);
        }

        double averageComputationCost = 0.0;

        for (Double cost : computationCosts.get(task).values()) {
            averageComputationCost += cost;
        }

        averageComputationCost /= computationCosts.get(task).size();
        System.out.println(" Average computation cost of the task " + task.getCloudletId() + " is "+ averageComputationCost );

        double max = 0.0;
        for (Task child : task.getChildList()) {
            double childCost = transferCosts.get(task).get(child)
                    + calculateUpwardRank(child);
            System.out.println(" Average transfer cost of the parent task " + task.getCloudletId() + "to child task"+ child.getCloudletId()+ " is "+ transferCosts.get(task).get(child) );
            max = Math.max(max, childCost);
        }
        u_rank.put(task, averageComputationCost + max);

        return u_rank.get(task);
    }

    /**
     * Allocates all tasks to be scheduled in non-ascending order of schedule.
     */
    private void allocateTasks() {
		  CustomVM CP_VM = find_Critical_processor();
	    	System.out.println("CT_ VM = "+ CP_VM.getId());
		  for (Task t :getTaskList()) {
		//	if(  (CT_Rank - rank.get(t)) < 0.0000009) {
	        	if (rank.get(t) >=( CT_Rank/*- 0.05*CT_Rank*/)) {
	        		System.out.println("CT_Task "+t.getCloudletId()+ t);
	        		 allocateCPTask(t, CP_VM);}}
	            
	        	 
//	       List<TaskRank> taskRank = new ArrayList<>();
//	        for (Task task : rank.keySet()) {
//	        if (rank.get(t)!= CT_Rank)
//	            taskRank.add(new TaskRank(t, rank.get(t)));
//	        }
//	        System.out.println(taskRank);
//	        // Sorting in non-ascending order of rank
//	        Collections.sort(taskRank);
//	       
//	        	
//	        	
//	        		 for (TaskRank Rank : taskRank) {
//	        		allocateTask(Rank.task);
//	        		//System.out.println("Other_Task "+Rank.task);
	        	   
	        	   
	        	   
	        	   List<TaskRank> taskRank = new ArrayList<>();
	        	 System.out.println("KEYSet "+ rank.keySet());
	               for (Task task : rank.keySet()) {
	            	   if (rank.get(task)!= CT_Rank)
    	            taskRank.add(new TaskRank(task, rank.get(task)));
	            	  
	               }
	               

	               // Sorting in non-ascending order of rank
	               Collections.sort(taskRank);
	               for (TaskRank rank : taskRank) {
	            	  
	                   allocateTask(rank.task);
	                   System.out.println("taskrank "+ rank.task);
	               }
//	               System.out.println(" Earliest Finish Time");
//	               System.out.println(earliestFinishTimes.size());
//	               earliestFinishTimes.forEach((key, value) -> System.out.println(key + " 	: 	" + key.getCloudletId()+ "	 : 	"+ key.getCloudletTotalLength() + "	 : 	"+ value));
//	            
	        }
	           
    void allocateCPTask(Task t, CustomVM CP_VM) {
    	System.out.println("in allocate CP task "+ t);
      	 CustomVM chosenVM = CP_VM;
      
           double earliestFinishTime = Double.MAX_VALUE;
           double bestReadyTime = 0.0;
           double finishTime;

          
               double minReadyTime = 0.0;

          
  
			for (Task parent : t.getParentList()) {
                   double readyTime = earliestFinishTimes.getOrDefault(parent, 0.0);
                   if (parent.getVmId() != CP_VM.getId()) {
                       readyTime += transferCosts.get(parent).get(t);
                   }
                   minReadyTime = Math.max(minReadyTime, readyTime);
               }

               finishTime = findFinishTime(t, CP_VM, minReadyTime, false);

               if (finishTime < earliestFinishTime) {
                   bestReadyTime = minReadyTime;
                   earliestFinishTime = finishTime;
                   chosenVM = CP_VM;
               }
               findFinishTime(t, chosenVM, bestReadyTime, true);
               earliestFinishTimes.put(t, earliestFinishTime);

               t.setVmId(chosenVM.getId());
       		
       	}

    
    public double calculateCT_Rank() {
        for (Task t :getTaskList()) {	
      	if(rank.get(t)> CT_Rank)
      			CT_Rank=rank.get(t);
      				}
        System.out.println("CT_Rank "+ CT_Rank);
        			return CT_Rank;  } 
  
    public  CustomVM  find_Critical_processor() { 
    	double minfinishTime= Double.MAX_VALUE;
    	CustomVM  critical_processor = null;
    	CustomVM vm = null;
    	 for (Object vmObject : getVmList()) {
             vm = (CustomVM) vmObject;
    		   double finishTime=0.0;
    		  
    	 for (Task t:getTaskList()) {
    		 double minReadyTime = 0.0;
    		 if(rank.get(t)== CT_Rank) {
    	          
    	         finishTime += findFinishTime(t, vm, minReadyTime, false); }
    	            }
    	 System.out.println("The finish time on " + vm.getId() + " VM is "+ finishTime);

         if (finishTime < minfinishTime) {
           minfinishTime=finishTime;
            critical_processor = vm; }
           
        
    	        }
    	 System.out.println("the Critical processor is "+ critical_processor.getId());
		return critical_processor;
    	}
    /**
     * Schedules the task given in one of the VMs minimizing the earliest finish
     * time
     *
     * @param task The task to be scheduled
     * @pre All parent tasks are already scheduled
     */
    private void allocateTask(Task task) {
    	
    	System.out.println("in allocate task "+ task);
        CustomVM chosenVM = null;
        double earliestFinishTime = Double.MAX_VALUE;
        double bestReadyTime = 0.0;
        double finishTime;

        for (Object vmObject : getVmList()) {
            CustomVM vm = (CustomVM) vmObject;
            double minReadyTime = 0.0;

            for (Task parent : task.getParentList()) {
                double readyTime = earliestFinishTimes.getOrDefault(parent, 0.0);
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
