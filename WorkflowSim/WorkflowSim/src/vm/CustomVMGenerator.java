package vm;

import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.workflowsim.CondorVM;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CustomVMGenerator {
    private static List<CondorVM> createCondorVM(int userId, int vms) {

        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<CondorVM> list = new LinkedList<>();

        //VM utils.Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        CondorVM[] vm = new CondorVM[vms];
        Random bwRandom = new Random(System.currentTimeMillis());
        for (int i = 0; i < vms; i++) {
            double ratio = 1;
            //double ratio = bwRandom.nextDouble();
            vm[i] = new CondorVM(i, userId, mips * ratio, pesNumber, ram, (long) (bw * ratio), size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }
        return list;
    }
    /**
     * Adds parameters like cost, freq, voltage based on type to the given condorvm
     * to return CustomVM
     */
    private static CustomVM getVm(int type, CondorVM vm) {
        double mips,cost,maxFreq,minFreq,minVoltage,maxVoltage,lambda;
        double costPerMem = 0.05;		// the cost of using memory in this vm
        double costPerStorage = 0.1;	// the cost of using storage in this vm
        double costPerBw = 0.1;
        
      //Add this on top.
        List<Double> FList = new ArrayList<>();
            double Pind = 0.0;
            double Cef = 0.0;
            double m = 0.0;
            double vcur = 0.0;
            double fcur = 0.0;

        
        
        
        if(type==0){
            mips=1000;
            cost=0.10;
            maxFreq=1.0;
            minFreq=0.26;
            minVoltage=5.0;
            maxVoltage=7.0;
            lambda=0.000150;
            FList.add(0.7);
            FList.add(maxFreq);
            FList.add(minFreq);
            Pind = 0.03;
            Cef = 0.80;
            m = 2.90;
            vcur = 0.7;
            fcur = 0.7;

        }
        else if(type==1){
            mips=1500;
            cost=0.20;
            maxFreq=1.0;
            minFreq=0.26;
            minVoltage=7.0;
            maxVoltage=9.0;
            lambda=0.000100;
            FList.add(0.7);
            FList.add(0.26);
            FList.add(1.0);
            Pind = 0.04;
            Cef = 0.8;
            m = 2.50;
            vcur = 0.7;
            fcur = 0.75;

        }

        else{
            mips=2000;
            cost=0.32;
            maxFreq=1.0;
            minFreq=.29;
            minVoltage=9.0;
            maxVoltage=11.0;
            lambda=0.000080;
            FList.add(0.75);
            FList.add(0.29);
            FList.add(1.0);
            Pind = 0.07;
            Cef = 1.0;
            m = 2.50;
            vcur = 0.75;
            fcur = 0.75;
        }

        
        //TODO: try removing the following 2 lines
        maxVoltage = 30;
        minVoltage = 15;
        CustomVM cvm = new CustomVM(vm,cost,costPerMem, costPerStorage, costPerBw, minFreq, maxFreq, minVoltage, maxVoltage, lambda, FList, Pind, Cef, m, vcur, fcur);
        return cvm;
    }

    /**
     * creates custom vms 
     * also look at getVm() function
     * @param userid
     * @param vms
     * @return
     */
    public static List<CustomVM> createCustomVMs(int userid, int vms){
        List<CustomVM> list = new ArrayList<>();

        //First create regular CondorVMs
        List<CondorVM> list0 = createCondorVM(userid, vms);
        

        CustomVM cvm;
        int type = 0;//type of vm
        for(CondorVM vm : list0) {
            type %= 3; //max number of types
            cvm = getVm(type, vm);
            type++;
            list.add(cvm);
        }
        return list;
    }
    

}

