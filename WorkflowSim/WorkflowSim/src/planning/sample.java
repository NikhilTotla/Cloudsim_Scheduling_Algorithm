//package planning;
//
//import org.apache.commons.math3.analysis.function.Max;
//import org.cloudbus.cloudsim.Consts;
//import org.cloudbus.cloudsim.power.PowerHost; 
//import org.cloudbus.cloudsim.Log;
//import org.workflowsim.CondorVM;
//import org.workflowsim.FileItem;
//import org.workflowsim.Job;
//import org.workflowsim.Task;
//import org.workflowsim.planning.BasePlanningAlgorithm;
//import org.workflowsim.utils.Parameters;
//import utils.Event;
//import utils.FromSchedules;
//import vm.CustomVM;
//
//import java.util.*;
//
//public abstract class BasePlanning extends BasePlanningAlgorithm {
//
//    public Map<Task, Map<CustomVM, Double>> computationCosts;
//    public Map<Task, Map<Task, Double>> transferCosts;
//    public Map<Task, Double> LFT;
//    public Map<Task, Double> earliestFinishTimes;   
//    public List<Task> CP;
//    public Map<Task, Double> earliestStartTimes;
//    public Map<Task, Double> etBest;
//    public Map<Task, Double> etAvg;
//    public Map<Task, Double> EET;
//    public Map<Task, Double> maximumEnergy;
//    public Map<Task, Double> minimumEnergy;
//    public Map<Task, Double> energyConsumptionLevel;
//    public Map<Task, Double> preassignedEnergyConsumption;
//    public double averageBandwidth;
//    public HashMap<Integer, CustomVM> id_vms;
//    private List<CustomVM> vms;
//    public Map<CustomVM, List<Event>> schedules;
//    public Double maximumEnergyWorkflow;
//    public Double minimumEnergyWorkflow;
//    
//    public Map<Task, Double> scheduledTask;
//    
//    
//    
//    
//    
//    
//    public BasePlanning() {
//        computationCosts = new HashMap<>();
//        transferCosts = new HashMap<>();
//        earliestFinishTimes = new HashMap<>();
//        schedules = new HashMap<>();
//    }
//
//    public void resetSchedules() {
//        schedules.clear();
//        for (CustomVM vm : getVmList()) {
//            schedules.put(vm, new ArrayList<>());
//        }
//    }
//
//    /**
//     * Calculates the average available bandwidth among all VMs in Mbit/s
//     *
//     * @return Average available bandwidth in Mbit/s
//     */
//    public double calculateAverageBandwidth() {
//        double avg = 0.0;
//        for (CustomVM vm : getVmList()) {
//            avg += vm.getBw();
//        }
//        return avg / getVmList().size();
//    }
//
//    /**
//     * Populates the computationCosts field with the time in seconds to compute
//     * a task in a vm.
//     */
//    public void calculateComputationCosts() {
//        for (Task task : getTaskList()) {
//            Map<CustomVM, Double> costsVm = new HashMap<>();
//            for (CustomVM vm : getVmList()) {
//                if (vm.getNumberOfPes() < task.getNumberOfPes()) {
//                    costsVm.put(vm, Double.MAX_VALUE);
//                } else {
//                    costsVm.put(vm,
//                            task.getCloudletTotalLength() / vm.getMips());
//                }
//            }
//            computationCosts.put(task, costsVm);
//        }
//    }
//
//    /**
//     * Populates the transferCosts map with the time in seconds to transfer all
//     * files from each parent to each child
//     */
//    public void calculateTransferCosts() {
//        // Initializing the matrix
//        for (Task task1 : getTaskList()) {
//            Map<Task, Double> taskTransferCosts = new HashMap<>();
//            for (Task task2 : getTaskList()) {
//                taskTransferCosts.put(task2, 0.0);
//            }
//            transferCosts.put(task1, taskTransferCosts);
//        }
//
//        // Calculating the actual values
//        for (Task parent : getTaskList()) {
//            for (Task child : parent.getChildList()) {
//                transferCosts.get(parent).put(child,
//                        calculateTransferCost(parent, child));
//            }
//        }
//    }
//
//    /**
//     * Accounts the time in seconds necessary to transfer all files described
//     * between parent and child
//     *
//     * @param parent
//     * @param child
//     * @return Transfer cost in seconds
//     */
//    public double calculateTransferCost(Task parent, Task child) {
//        List<FileItem> parentFiles = parent.getFileList();
//        List<FileItem> childFiles = child.getFileList();
//
//        double acc = 0.0;
//
//        for (FileItem parentFile : parentFiles) {
//            if (parentFile.getType() != Parameters.FileType.OUTPUT) {
//                continue;
//            }
//
//            for (FileItem childFile : childFiles) {
//                if (childFile.getType() == Parameters.FileType.INPUT
//                        && childFile.getName().equals(parentFile.getName())) {
//                    acc += childFile.getSize();
//                    break;
//                }
//            }
//        }
//
//
//        //file Size is in Bytes, acc in MB
//        acc = acc / Consts.MILLION;
//        // acc in MB, averageBandwidth in Mb/s
//        return acc * 8 / averageBandwidth;
//    }
//
//    public double getStartTime(Task task) {
//        double dataTransfer = 0.0;
//        double computationCost = computationCosts.get(task).get(id_vms.get(task.getVmId()));
//        for (Task parent : task.getParentList()) {
//            if (parent.getVmId() != task.getVmId()) {
//                dataTransfer += transferCosts.get(parent).get(task);
//            }
//        }
//        return getFinishTime(task) - dataTransfer - computationCost;
//    } 
//    public double getFinishTime(Task task) {
//        return earliestFinishTimes.get(task);
//    }
//
//    @Override
//    public void setVmList(List vmList) {
//        this.vms = new LinkedList<>();
//        this.id_vms = new HashMap<>();
//        for(Object vm: vmList) {
//            vms.add((CustomVM) vm);
//            id_vms.put(((CustomVM) vm).getId(), (CustomVM) vm);
//        }
//    }
//
//    @Override
//    public List<CustomVM> getVmList() {
//        return vms;
//    }
//
//
//    /**
//     * Finds the best time slot available to minimize the finish time of the
//     * given task in the vm with the constraint of not scheduling it before
//     * readyTime. If occupySlot is true, reserves the time slot in the schedule.
//     *
//     * @param task The task to have the time slot reserved
//     * @param vm The vm that will execute the task
//     * @param readyTime The first moment that the task is available to be
//     * scheduled
//     * @param occupySlot If true, reserves the time slot in the schedule.
//     * @return The minimal finish time of the task in the vmn
//     */
//    public double findFinishTime(Task task, CustomVM vm, double readyTime,
//                                 boolean occupySlot) {
//        List<Event> sched = schedules.get(vm);
//        double computationCost = computationCosts.get(task).get(vm);
//        double start, finish;
//        int pos;
//
//        if (sched.isEmpty()) {
//            if (occupySlot) {
//                sched.add(new Event(readyTime, readyTime + computationCost, task, vm));
//            }
//            return readyTime + computationCost;
//        }
//
//        if (sched.size() == 1) {
//            if (readyTime >= sched.get(0).finish) {
//                pos = 1;
//                start = readyTime;
//            } else if (readyTime + computationCost <= sched.get(0).start) {
//                pos = 0;
//                start = readyTime;
//            } else {
//                pos = 1;
//                start = sched.get(0).finish;
//            }
//
//            if (occupySlot) {
//                sched.add(pos, new Event(start, start + computationCost, task, vm));
//            }
//            return start + computationCost;
//        }
//
//        // Trivial case: Start after the latest task scheduled
//        start = Math.max(readyTime, sched.get(sched.size() - 1).finish);
//        finish = start + computationCost;
//        int i = sched.size() - 1;
//        int j = sched.size() - 2;
//        pos = i + 1;
//        while (j >= 0) {
//            Event current = sched.get(i);
//            Event previous = sched.get(j);
//
//            if (readyTime > previous.finish) {
//                if (readyTime + computationCost <= current.start) {
//                    start = readyTime;
//                    finish = readyTime + computationCost;
//                }
//
//                break;
//            }
//            if (previous.finish + computationCost <= current.start) {
//                start = previous.finish;
//                finish = previous.finish + computationCost;
//                pos = i;
//            }
//            i--;
//            j--;
//        }
//
//        if (readyTime + computationCost <= sched.get(0).start) {
//            pos = 0;
//            start = readyTime;
//
//            if (occupySlot) {
//                sched.add(pos, new Event(start, start + computationCost, task, vm));
//            }
//            return start + computationCost;
//        }
//        if (occupySlot) {
//            sched.add(pos, new Event(start, finish, task, vm));
//        }
//        return finish;
//    }
//   // public void printMetrics() {
//       // FromSchedules.logInfo(schedules);
//    	public  void printMetrics(List<Job> jobs, List<CustomVM> vms) {
//    	    
//            Log.printLine("Makespan : " + getMKSP(jobs));
//            Log.printLine("Energy : " + getEnergyConsumed(jobs, vms));
//            Log.printLine("Utilisation : " + getUtil(jobs, vms));
//            Log.printLine("Cost : " +  getCost(vms, jobs));
//            Log.printLine("===============================================");
//        }
// //   }
//    public void copyTo(BasePlanning to) {
//        to.schedules = schedules;
//        to.id_vms = id_vms;
//        to.earliestFinishTimes = earliestFinishTimes;
//        to.transferCosts = transferCosts;
//        to.computationCosts = computationCosts;
//        to.averageBandwidth = averageBandwidth;
//    }
//    
//    
//    public static double getMKSP(List<Job> jobs) {
//        double start = Double.MAX_VALUE, end = Double.MIN_VALUE;
//        for(Job j:jobs) {
//            start = Math.min(start, j.getExecStartTime());
//            //Log.printLine("The start time for job "+ j + "is"+ start);
//            end = Math.max(end, j.getFinishTime());
//           // Log.printLine("The end time for job "+ j + "is"+ end);
//        } //Log.printLine("The  start and end time overall are "+ start + "and "+ end);
//        return end - start;
//        
//    }
//
//    /**
//     *  cost = sum of time spent in vm by a task*cost per sec over all tasks and total transfer costs
//     * @param vms
//     * @param jobs
//     * @return
//     */
//    private static double getCost(List<CustomVM> vms, List<Job> jobs) {
//        double cost = 0;
//        for(Job j: jobs) {
//            //Get the vm this job is executing on
//            CondorVM vm = null;
//            for(CondorVM cvm:vms) {
//                if(cvm.getId() == j.getVmId()) {
//                    vm = cvm;
//                    break;
//                }
//            }
//            assert vm != null;
//
//            //cost for execution on vm
//            cost += j.getActualCPUTime() * vm.getCost();
//            
//            //cost for file transfer on this vm
//            long fileSize = 0;
//            for (FileItem file : j.getFileList()) {
//                fileSize += file.getSize() / Consts.MILLION;
//            }
//            cost += vm.getCostPerBW() * fileSize;
//        }
//        return cost;
//    }
//    
//    /**
//     * returns power consumed when running at given frequency
//     * @param vm
//     * @param runningFreq
//     * @return
//     */
//    public static double getPower(CustomVM vm, double runningFreq) {
//        double runningVolt = vm.getMinVolt() +
//                (vm.getMaxVolt() - vm.getMinVolt()) * (runningFreq - vm.getMinFreq()) / (vm.getMaxFreq() - vm.getMinFreq());
//        return (runningFreq*runningVolt*runningVolt);
//    }
//
//    /**
//     * returns utilisation 
//     * obtained by [ sum(activetime / makespan) of all vms ] / number of vms used
//     * @param jobs
//     * @param vms
//     * @return
//     */
//    public static double getUtil(List<Job> jobs,List<CustomVM> vms)
//    {
//        double util = 0.0, mksp = getMKSP(jobs);
//        int vmsz = 0;
//        HashMap<Integer, Double> actv_tm = new HashMap<>(); //calculating active times
//        //initialization
//        for(CustomVM vm:vms)
//            actv_tm.put(vm.getId(), 0.);
//        // get active times of each vm
//        for(Job j:jobs) {
//            CustomVM vm = null;
//            for(CustomVM cvm:vms)
//                if(cvm.getId() == j.getVmId()) {
//                    vm = cvm;
//                    break;
//                }
//            actv_tm.put(vm.getId(), actv_tm.get(j.getVmId()) + j.getActualCPUTime() );
//        }
//        //Number of active vms
//        for(CustomVM vm:vms) {
//            if(vm.isPowerOn()) {
//                vmsz++;
//            }
//            util += actv_tm.get(vm.getId())/mksp;
//        }
//        if(vmsz != 0) {
//            util /= vmsz;
//            util *= 100;
//        }
//
//        return util;
//    }
//
//    /**
//     * gets energy
//     * @param jobs
//     * @param vms
//     * @return
//     */
//    private static double getEnergyConsumed(List<Job> jobs,List<CustomVM> vms) {
//        double energy = 0.0;
//        HashMap<Integer, Double> actv_tm = new HashMap<>(); //calculating active times
//        //initialization
//        for(CustomVM vm:vms)
//            actv_tm.put(vm.getId(), 0.);
//        for(Job j:jobs) {
//            //get the vm running this job
//            CustomVM vm = null;
//            for(CustomVM cvm:vms)
//                if(cvm.getId() == j.getVmId()) {
//                    vm = cvm;
//                    break;
//                }
//            assert vm != null;
//            energy += (getPower(vm, vm.getMaxFreq())*j.getActualCPUTime());
//            actv_tm.put(vm.getId(), actv_tm.get(vm.getId())+ j.getCloudletLength()/vm.getMips());
//
//        }
//        double mksp = getMKSP(jobs);
//        //slack time costs
//        for(CustomVM vm:vms) {
//            if(vm.isPowerOn()) {
//                energy += (getPower(vm, vm.getMinFreq())*(mksp - actv_tm.get(vm.getId())));
//            }
//        }
//
//        return energy;
//    }
//    
//    private static double rk = 10; //rk is not defined in whole paper, property of host.
//    private static double pkmax = 1000; //Property of host.
//    private static double fkmax = 1000; //Property of host.
//    private static double fk = 10;  //Property of host. 
//    
//    
//    //Get Tasks associated with a particular VM
//    
//    private static List<Task> getTasksForVM(CustomVM curvm, List<Task> tasks) {
//        List<Task> tasksForVM = new ArrayList<>();
//
//        for (Task task : tasks) {
//            if (task.getVmId() == curvm.getId()) {
//                tasksForVM.add(task);
//            }
//        }
//
//        return tasksForVM;
//    }
//    
//  //Finish time of a virtual machine
//    
//    private double getFinishTimeVM (CustomVM curVm) {
//    	double ft = 0;
//    	
//    	
//    	List<Task> taskCurVM = getTasksForVM(curVm, getTaskList());
//    	
//    	for(Task t: taskCurVM) {
//    		double temp = getFinishTime(t);
//    		if(ft<temp) {
//    			ft = temp;
//    		}
//    	}
//    	
//    	
//    	return ft;
//    	
//    }
//    
//    //Start time of a virtual machine
//    
//    private double getStartTimeVM (CustomVM curVm) {
//    	double st = Double.MAX_VALUE;
//    	
//    	List<Task> taskCurVM = getTasksForVM(curVm, getTaskList());
//    	
//    	for(Task t: taskCurVM) {
//    		double temp = getStartTime(t);
//    		if(st>temp) {
//    			st = temp;
//    		}
//    	}
//    	st -= curVm.getStartTime();
//    	
//    	
//    	return st;
//    }
//    
//    //Get Energy consumption of a Virtual machine
//    private double getEnergyVM(CustomVM curVm) {
//    	double energy = 0;
//    	
//    	energy = ((1-rk)* pkmax * Math.pow(fk, 3) * (getFinishTimeVM(curVm) - getStartTimeVM(curVm)))/(Math.pow(fkmax, 3));
//    	
//    	
//    	return energy;
//    }
//    
//    //Get Earliest finish time. To adjust initial priority of tasks
//    //ET is directly proportional to CC and TT is proportional to TC. So can be used interchangeably
//    
//    //EFT of task
//    
//    private double getEFTTask(Task t) {
//    	double eft = 0;
//    	
//    	List<Task> parentList = t.getParentList();
//    	
//    	//Check if task is entry task
//    	if(parentList.size()==0) {
//    		eft = computationCosts.get(t).get(id_vms.get(t.getVmId()));
//    	} else {
//    		for(Task tl: parentList) {
//    			
//    			double temp = getEFTTask(tl) + transferCosts.get(tl).get(t);
//    			
//    			if(temp>eft) {
//    				eft = temp;
//    			}
//    			
//    		}
//    	}
//    	
//    	eft+=computationCosts.get(t).get(id_vms.get(t.getVmId()));
//    	
//    	return eft;
//    }
//    
//    //Returns Initial priority of tasks for Algorithm 2
//    
//    public List<Task> getTasksInIncreasingEFTOrder() {
//        List<Task> tasks = new ArrayList<>(getTaskList());
//
//        // Sort the tasks based on their EFT using a custom comparator
//        Collections.sort(tasks, new Comparator<Task>() {
//            @Override
//            public int compare(Task task1, Task task2) {
//                double eft1 = getEFTTask(task1);
//                double eft2 = getEFTTask(task2);
//                return Double.compare(eft1, eft2);
//            }
//        });
//
//        return tasks;
//    }
//    
//    
//    //Reliability paper
//    
//    //Energy consumed by task ti running on vmk (ET)
//    public double getET(Task t, CustomVM vm, double fh) {
//    	
//    	Double cost = computationCosts.get(t).get(vm);
//    	cost = (cost * vm.getMaxFreq())/fh;
//    	
//    	return cost;
//    	
//    }
//    
//    //Returns energy for a task ti running on virtual machine vmk. (E)
//    
//    public double getEnergyForVM(Task t, CustomVM vm, double fh) {    	
//    	double energy = (vm.getPind()+ (vm.getCef() * Math.pow(fh, vm.getM())))*getET(t,vm,fh);
//    	
//    	
//    	return energy;
//    }
//    
//    
//    
//    public double getEarliestStartTime(Task task) {
////        double dataTransfer = 0.0;
////        double computationCost = computationCosts.get(task).get(id_vms.get(task.getVmId()));
////        for (Task parent : task.getParentList()) {
////            if (parent.getVmId() != task.getVmId()) {
////                dataTransfer += transferCosts.get(parent).get(task);
////            }
////        }
////        return getFinishTime(task) - dataTransfer - computationCost;
//    	if(earliestStartTimes.containsKey(task)) return earliestStartTimes.get(task);
//    	Double est = 0.0;
//    	for(Task parent : task.getParentList())
//    	{
//    		est = Math.max(getEarliestStartTime(parent) + transferCosts.get(parent).get(task)+  etBest.get(task),est);
//    		
//    	}
//    	earliestStartTimes.put(task, est);
//    	return est ;
//    	
//    } 
//    
//    //Calculates ET Best for each task and store it in a map
//    // fh is the current frequency being used. Change it accordingly
//    public void calculateETBest(Task t) {
//    	Double ans = Double.MAX_VALUE;
//    	Double fh = 10.0;
//    	
//    	Double sum = 0.0;
//    	
//    	for(CustomVM Vm : getVmList()) {
//    		ans = Math.min(ans, getET(t, Vm, fh));
//    		sum+= getET(t, Vm, fh);
//    		etBest.put(t, ans) ;
//    	}
//    	etAvg.put(t, sum/getVmList().size());
//    }
//    
//    public void calculateLFT(Double D, Task t)
//    {
//    	if(t.getParentList().size() == 0) LFT.put(t, D);
//    	else
//    	{
//    		LFT.put(t, earliestStartTimes.get(t) + etBest.get(t));
//    		
//    	}
//    		
//    }
//    public void calculateCP()
//    {
//    	Task t=null;
//    	for(Task task : getTaskList())
//    	{
//    		if(task.getChildList().size() == 0)
//    		{
//    			t = task;
//    			break;
//    		}
//    	}
//
//    	while(t.getParentList().size() != 0)
//    	{	
//    		
//    		Double len = 0.0;
//    		Task Par = null;
//    		for(Task parent : t.getParentList())
//    		{
//    			if(len < LFT.get(parent)) {
//    				len = LFT.get(parent);
//    				Par = parent;
//    			}
//    		}
//    		CP.add(t);
//    		t = Par;
//    	}
//    	CP.add(t);
//    }
//    
//    public void calculateEET() {
//    	Task tExit = null;
//    	Task tEntry = null;
//    	for(Task t: getTaskList()) {
//    		if(t.getParentList().size()==0) tEntry = t;
//    		if(t.getChildList().size()==0) tExit = t;
//    		
//    	}
//    	
//    	Double sum = 0.0;
//    	for(Task t: CP) {
//    		sum += etAvg.get(t);
//    	}
//    	
//    	Double lengthCP = 0.0;
//    	for(int i = 1; i<CP.size(); i++) {
//    		lengthCP += transferCosts.get(CP.get(i)).get(CP.get(i-1));
//    	}
//    	Double scp = (LFT.get(tExit) - earliestStartTimes.get(tEntry) - lengthCP-sum)/sum;
//    	
//    	for(Task t: CP) {
//    		Double eet = etAvg.get(t) + (scp * etAvg.get(t));
//    		EET.put(t, eet);
//    	}
//    }
//    
//    //Calculates maximum energy consumed by a task 
//    
//    public void calculateMaximumEnergyTask() {
//    	for(Task t: getTaskList()) {
//    		double energy = 0.0;
//    		for(CustomVM vm: getVmList()) {
//    			energy = Math.max(energy, getEnergyForVM(t, vm, vm.getMaxFreq()));
//    		}
//    		maximumEnergy.put(t, energy);
//    	}
//    }
//    
//    //Calculates minimum energy consumed by a task
//    
//    public void calculateMinumumEnergyTask() {
//    	for(Task t: getTaskList()) {
//    		double energy = Double.MAX_VALUE;
//    		for(CustomVM vm: getVmList()) {
//    			energy = Math.min(energy, getEnergyForVM(t, vm, vm.getMinFreq()));
//    		}
//    		minimumEnergy.put(t, energy);
//    	}
//    }
//    
//    //Calculate minimum and maximum energy of workflow
//    
//    public void energyWorkflow() {
//    	for(Double energy: maximumEnergy.values() ) {
//    		maximumEnergyWorkflow+=energy;
//    	}
//    	for(Double energy: minimumEnergy.values() ) {
//    		minimumEnergyWorkflow+=energy;
//    	}
//    }
//    
//    //Calculate energy consumption level ( 34)
//    
//    public void calculateEnergyConsumptionLevel() {
//    	for(Task t: getTaskList()) {
//    		double energy = (maximumEnergy.get(t) + minimumEnergy.get(t))/(maximumEnergyWorkflow+minimumEnergyWorkflow);
//    		energyConsumptionLevel.put(t, energy);
//    	}
//    }
//    
//    //Calculates preassigned energy consumption level. Given energy consumption of workflow as input
//    //35
//    
//    public void calculatePreassignedEnergyConsumption(Double eG) {
//    	for(Task t: getTaskList()) {
//    		double temp = (eG - minimumEnergyWorkflow) * energyConsumptionLevel.get(t) + minimumEnergyWorkflow;
//    		double energy = Math.min(maximumEnergy.get(t), temp);
//    		preassignedEnergyConsumption.put(t, energy);
//    	}
//    }
//    
//    
//    //In the next few functions, R is being calculated
//    
//    //Calculating average transient rate
//    
//    Double d= 1.0; //It is the sensitivity of the fault arrival rate of working frequency
//    
//    Double aarMax = 2.0; //Average arrival rate at maximum frequency
//    
//    
//    //15
//    public Double getAAR(CustomVM vm, Double fh) {
//    	Double aar = aarMax;
//    	
//    	Double power = d* (vm.getMaxFreq()-fh) / (vm.getMaxFreq() - vm.getMinFreq());
//    	
//    	aar *= Math.pow(10, power);
//    	
//    	
//    	return aar;
//    }
//    
//    //Et worst and L (pending to write)
//    
//    public Double getEtWorst(CustomVM vm, Task t, Double fh) {
//    	Double energy = 10.0;
//    	
//    	return energy;
//    }
//    
//    public Double getL(CustomVM vm, Task t, Double fh) {
//    	Double l = 10.0;
//    	
//    	return l;
//    }
//    
//    
//    
//    //Probability of transient failure of processor following poisons distribution
//    //14
//    
//    //Probability of g transient faults occurring. 
//    
//    public Double getPg(Task t, CustomVM vm, Double fh, int g) {
//    	
//    	Double factg = 1.0;
//        for(int i = 2;i<=g;i++) {
//        	factg= factg*i;
//        }
//        
//        Double ret = 1/factg;
//        
//        ret*= Math.pow(Math.E, -1*(getAAR(vm,fh)*(getEtWorst(vm, t, fh))) );
//        ret*= Math.pow((getAAR(vm,fh)*(getEtWorst(vm, t, fh))), g );
//        
//        return ret;
//    }
//    
//    // Calculating Rg
//    //16
//    public Double getRg(Task t, CustomVM vm, Double fh, int g) {
//    	Double rg = getPg(t, vm, fh, g) * Math.pow(Math.E, -1*(getAAR(vm,fh)* getL(vm,t, fh)));
//    	return rg;
//    }
//    
//    //Calculating Reliablity
//    //17
//    
//    
//    public Double getReliablity(Task t, CustomVM vm, Double fh) {
//    	Double ret = 0.0;
//    	for(int i = 0;i<=Ku; i++) {
//    		ret += getRg(t, vm, fh, i);
//    	}
//    	
//    	return ret;
//    }
//    
//    
//    
//
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//
//    
//    
//    
//  
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//
//}
