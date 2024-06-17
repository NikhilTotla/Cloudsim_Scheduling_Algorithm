package vm;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.CondorVM;
import java.util.List;

public class CustomVM extends CondorVM {
    private double maxFreq, minFreq;
    private double maxVolt, minVolt;
    private boolean powerOn = true;
    private double lambda = 0.000015;
    private double startTime; //Added startTime variable to calculate starttime in BasePlanning
    private List<Double> VList;  // List of voltages
    private List<Double> FList;  // List of frequencies
    private double Pind;  // Pind value
    private double Cef;   // Cef value
    private double m;
    private double vcur, fcur;


    CustomVM(
            int id,
            int userId,
            double mips,
            int numberOfPes,
            int ram,
            long bw,
            long size,
            String vmm,
            CloudletScheduler cloudletScheduler,
            List<Double> VList,
            List<Double> FList,
            double Pind,
            double Cef,
            double m
    ) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm,cloudletScheduler);
        this.startTime = CloudSim.clock(); //Sets current time as starttime
        this.VList = VList;//For reliability paper
        this.FList = FList; 
        this.Pind = Pind;
        this.Cef = Cef;
        this.m = m;

    }

    CustomVM(CustomVM vm) {
        this((CondorVM) vm);
        this.setFreqRange(vm.getMinFreq(), vm.getMaxFreq());
        this.setVoltRange(vm.getMinVolt(), vm.getMaxVolt());
    }

    CustomVM(CondorVM vm) {
        super(vm.getId(), vm.getUserId(), vm.getMips(), vm.getNumberOfPes(),
                vm.getRam(), vm.getBw(), vm.getSize(), vm.getVmm(), vm.getCloudletScheduler());
    }
    CustomVM(CondorVM vm, double cost, double costPerMem, double costPerStorage, double costPerBW) {
        super(vm.getId(), vm.getUserId(), vm.getMips(), vm.getNumberOfPes(),
                vm.getRam(), vm.getBw(), vm.getSize(), vm.getVmm(),
                cost, costPerMem, costPerStorage, costPerBW, vm.getCloudletScheduler());
    }
    CustomVM(CondorVM vm,double cost, double costPerMem,double costPerStorage, double costPerBw, double minFreq, double maxFreq, double minVoltage, double maxVoltage, double lambda, List<Double>FList, double Pind, double Cef, double m,double vcur, double fcur)
    {
        this(vm, cost, costPerMem, costPerStorage, costPerBw);
        this.setFreqRange(minFreq, maxFreq);
        this.setVoltRange(minVolt, maxVolt);
//        this.mips = mips;
        this.lambda = lambda;
        this.FList = FList;
        this.Pind = Pind;
        this.Cef = Cef;
        this.m = m;
        this.vcur = vcur;
        this.fcur = fcur;
    }
    public void setFreqRange(double minFreq, double maxFreq) {
        this.maxFreq = maxFreq;
        this.minFreq = minFreq;
    }
    public double getMaxFreq() {
        return maxFreq;
    }
    public double getMinFreq() {
        return minFreq;
    }
    public void setVoltRange(double minVolt, double maxVolt) {
        this.maxVolt = maxVolt;
        this.minVolt = minVolt;
    }
    public double getMaxVolt() {
        return maxVolt;
    }
    public double getMinVolt() {
        return minVolt;
    }
    public void shutDown() {
        this.powerOn = false;
    }
    public void turnPowerOn() {
        this.powerOn = true;
    }

    public double getLambda(double runningFreq) {
        return lambda * Math.pow(10, (runningFreq - 1) / (this.getMinFreq() - 1));
    }

    public boolean isPowerOn() {
        return powerOn;
    }
    
    //Gets starttime for the current instance of VM
    public double getStartTime() {
        return startTime;
    }
    
    //For reliability paper
    public List<Double> getVList() {
        return VList;
    }

    public List<Double> getFList() {
        return FList;
    }
    
    public double getPind() {
        return Pind;
    }

    public double getCef() {
        return Cef;
    }
    
    public double getM () {
    	return m;
    }
    
    public double getCurrentVoltage() {
    	return vcur;
    }
    public double getCurrentFrequency() {
    	return fcur;
    }
}