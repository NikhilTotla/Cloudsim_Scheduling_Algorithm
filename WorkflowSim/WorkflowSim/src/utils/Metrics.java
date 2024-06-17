package utils;

import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.Log;
import org.workflowsim.CondorVM;
import org.workflowsim.FileItem;
import org.workflowsim.Job;
import vm.CustomVM;

import java.util.HashMap;
import java.util.List;

public class Metrics {
    /**
     * total time for execution
     * obtained by : finish time of last job - start time of first job executed
     * @param jobs
     * @return
     */
    public static double getMKSP(List<Job> jobs) {
        double start = Double.MAX_VALUE, end = Double.MIN_VALUE;
        for(Job j:jobs) {
            start = Math.min(start, j.getExecStartTime());
            Log.printLine("The start time for job "+ j + "is"+ start);
            end = Math.max(end, j.getFinishTime());
            Log.printLine("The end time for job "+ j + "is"+ end);
        } Log.printLine("The  start and end time overall are "+ start + "and "+ end);
        return end - start;
        
    }

    /**
     *  cost = sum of time spent in vm by a task*cost per sec over all tasks and total transfer costs
     * @param vms
     * @param jobs
     * @return
     */
    private static double getCost(List<CustomVM> vms, List<Job> jobs) {
        double cost = 0;
        for(Job j: jobs) {
            //Get the vm this job is executing on
            CondorVM vm = null;
            for(CondorVM cvm:vms) {
                if(cvm.getId() == j.getVmId()) {
                    vm = cvm;
                    break;
                }
            }
            assert vm != null;

            //cost for execution on vm
            cost += j.getActualCPUTime() * vm.getCost();
            
            //cost for file transfer on this vm
            long fileSize = 0;
            for (FileItem file : j.getFileList()) {
                fileSize += file.getSize() / Consts.MILLION;
            }
            cost += vm.getCostPerBW() * fileSize;
        }
        return cost;
    }
    
    /**
     * returns power consumed when running at given frequency
     * @param vm
     * @param runningFreq
     * @return
     */
    public static double getPower(CustomVM vm, double runningFreq) {
        double runningVolt = vm.getMinVolt() +
                (vm.getMaxVolt() - vm.getMinVolt()) * (runningFreq - vm.getMinFreq()) / (vm.getMaxFreq() - vm.getMinFreq());
        return (runningFreq*runningVolt*runningVolt);
    }

    /**
     * returns utilisation 
     * obtained by [ sum(activetime / makespan) of all vms ] / number of vms used
     * @param jobs
     * @param vms
     * @return
     */
    public static double getUtil(List<Job> jobs,List<CustomVM> vms)
    {
        double util = 0.0, mksp = getMKSP(jobs);
        int vmsz = 0;
        HashMap<Integer, Double> actv_tm = new HashMap<>(); //calculating active times
        //initialization
        for(CustomVM vm:vms)
            actv_tm.put(vm.getId(), 0.);
        // get active times of each vm
        for(Job j:jobs) {
            CustomVM vm = null;
            for(CustomVM cvm:vms)
                if(cvm.getId() == j.getVmId()) {
                    vm = cvm;
                    break;
                }
            actv_tm.put(vm.getId(), actv_tm.get(j.getVmId()) + j.getActualCPUTime() );
        }
        //Number of active vms
        for(CustomVM vm:vms) {
            if(vm.isPowerOn()) {
                vmsz++;
            }
            util += actv_tm.get(vm.getId())/mksp;
        }
        if(vmsz != 0) {
            util /= vmsz;
            util *= 100;
        }

        return util;
    }

    /**
     * gets energy
     * @param jobs
     * @param vms
     * @return
     */
    private static double getEnergyConsumed(List<Job> jobs,List<CustomVM> vms) {
        double energy = 0.0;
        HashMap<Integer, Double> actv_tm = new HashMap<>(); //calculating active times
        //initialization
        for(CustomVM vm:vms)
            actv_tm.put(vm.getId(), 0.);
        for(Job j:jobs) {
            //get the vm running this job
            CustomVM vm = null;
            for(CustomVM cvm:vms)
                if(cvm.getId() == j.getVmId()) {
                    vm = cvm;
                    break;
                }
            assert vm != null;
            energy += (getPower(vm, vm.getMaxFreq())*j.getActualCPUTime());
            actv_tm.put(vm.getId(), actv_tm.get(vm.getId())+ j.getCloudletLength()/vm.getMips());

        }
        double mksp = getMKSP(jobs);
        //slack time costs
        for(CustomVM vm:vms) {
            if(vm.isPowerOn()) {
                energy += (getPower(vm, vm.getMinFreq())*(mksp - actv_tm.get(vm.getId())));
            }
        }

        return energy;
    }


    public static void printMetrics(List<Job> jobs, List<CustomVM> vms) {
    
        Log.printLine("Makespan : " + getMKSP(jobs));
        Log.printLine("Energy : " + getEnergyConsumed(jobs, vms));
        Log.printLine("Utilisation : " + getUtil(jobs, vms));
        Log.printLine("Cost : " +  getCost(vms, jobs));
        Log.printLine("===============================================");
    }


}
