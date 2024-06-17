package utils;

import org.cloudbus.cloudsim.Log;
import org.workflowsim.CondorVM;
import org.workflowsim.Task;
import vm.CustomVM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FromSchedules {
    public static double getMKSP(Map<CustomVM, List<Event>> schedules) {
        double start=Double.MAX_VALUE,end=Double.MIN_VALUE;
        //Log.printLine(vms.size());
        for(CustomVM vm : schedules.keySet()) {
            List<Event> tasks= schedules.get(vm);
            for(Event e : tasks) {
                start = Math.min(start, e.start);
                Log.printLine("The start time for  job "+ e + "on the vm " + vm.getId() + " is "+ start);
                end = Math.max(end, e.finish);
               Log.printLine("The end time for job "+ e + "is"+ end);
               System.out.println();
            }
        } Log.printLine("The  start and end time overall are "+ start + "and "+ end);
        return end-start;
    }
    public static double getEnergy(Map<CustomVM, List<Event>> schedules) {
        double nrg = 0.0, mksp = getMKSP(schedules);

        for(CustomVM vm: schedules.keySet()) {
            if(schedules.get(vm) == null || schedules.get(vm).size() == 0)
                continue;

            double power = 0, at = 0.0;

            for(Event e: schedules.get(vm)) {
                power = Metrics.getPower(vm, e.runFreq);
                //nrg += power * (e.finish - e.start);
                //since we keep vm at given freq only during execution (not during datatransfer)
                double runTime = e.task.getCloudletLength()/vm.getMips();
                nrg += power * runTime;
                at += runTime;
            }

            if(vm.isPowerOn()) {
                nrg += Metrics.getPower(vm, vm.getMinFreq()) * (mksp - at);
            }
        }
        return nrg;
    }
    public static double getActiveTime(CustomVM vm, Map<CustomVM, List<Event>> schedules) {
        double at = 0.0;
        for(Event e: schedules.get(vm)) {
            at += (e.finish - e.start);
        }
        return at;
    }

    /**
     * Same implementation as in MHEFTPlanningAlgorithmExample2
     */
    public static double getUtilisation(Map<CustomVM, List<Event>> schedules) {

        double util = 0.0, mksp = getMKSP(schedules);
        int vmsz = 0;

        //Number of active vms
        for(CustomVM vm: schedules.keySet()) {
            if(vm.isPowerOn()) {
                vmsz++;
                util += getActiveTime(vm, schedules) / mksp;
            }
        }
        if(vmsz != 0) {
            util /= vmsz;
            util *= 100;
        }

        return util;

    }

    public static double getCost(Map<CustomVM, List<Event>> schedules) {
        double cost = 0;
        for(CustomVM vm: schedules.keySet()) {
            double at = getActiveTime(vm, schedules);
            cost += at * vm.getCost();
        }
        return cost;
    }




    public static void logInfo(Map<CustomVM, List<Event>> schedules) {

        Log.printLine("Makespan : " + getMKSP(schedules));
        Log.printLine("Energy : " + getEnergy(schedules));
        Log.printLine("Utilisation : " + getUtilisation(schedules));
        Log.printLine("Cost : " + getCost(schedules));
        Log.printLine("===============================================");

    }
}
