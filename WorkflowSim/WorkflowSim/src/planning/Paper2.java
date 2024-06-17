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

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.workflowsim.Task;


import utils.TaskRank;
import vm.CustomVM;


/**
 * The HEFT planning algorithm.
 *
 * @author Pedro Paulo Vezzá Campos
 * @date Oct 12, 2013
 */
public class Paper2 extends BasePlanning {


	
//	public Map<Task, Double> taskToR;
//	Double tempEg = 72.9;
    public Paper2() {
//        rank = new HashMap<>();
//    	taskToVm = new HashMap<>();
//    	taskToFrequency = new HashMap<>();
//    	taskToR = new HashMap<>();
    	
    }

    /**
     * The main function
     */
    @Override
    public void run() {
        Log.printLine("Paper2 planner running with " + getTaskList().size()
                + " tasks and " + getVmList().size() + "vms.");
        averageBandwidth = calculateAverageBandwidth();
        System.out.println("The average Bamdwidth is = "+ averageBandwidth);
        resetSchedules();
//        ALGO 1
        calculateComputationCosts();
//        calcETworst();
        calculateTransferCosts();

//        ALGO 2
        
        for(Task t : getTaskList()) 
        {
        	calculateETBest(t);
        }
        for(Task t : getTaskList()) 
        {
        	getEarliestStartTime(t);
        }
        for(Task t : getTaskList()) 
        {
        	calculateLFT(t);
        }
        
        calculateCP();
//        for(Task t : CP)
//      	{System.out.println(t.getCloudletId());}
        calculateEET();
        
        recalculateEST();
        
        calcDeadlineCP();
        
        calcLFT();

//        System.out.println(Double.MAX_VALUE);
//        ALGO 3
        calculateMaximumEnergyTask();
        calculateMinumumEnergyTask();
        energyWorkflow();
        calculateEnergyConsumptionLevel();
        calculatePreassignedEnergyConsumption();
        
//        

        for(Task t : getTaskList())
        {
        	calculateRank(t);
        }
        calculatetask_list();
//        for(Task t : task_list)
        {
//        	for(CustomVM vm: getVmList())
        	{
//        		System.out.println(getAAR(vm, vm.getCurrentFrequency()));
//        		System.out.println(getReliablity(t,vm, vm.getCurrentFrequency()));
//        		System.out.println(calcETworst(t, vm, vm.getCurrentFrequency()));
        	}
        }
        Algorithm3();
        System.out.println("Total Energy = " + getEnergy());
        System.out.println("Reliability = "+ getreliability());
//        for(Task t : task_list)
//        {
//          	for(CustomVM vm : getVmList())
//        	if(rank.containsKey(t))
//          	{
//              	System.out.println(t.getCloudletId());
//          	}
//        }
//        System.out.println(maximumEnergyWorkflow);
        
    }

    }

