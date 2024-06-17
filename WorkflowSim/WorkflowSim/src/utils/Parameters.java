package utils;


import planning.*;

public interface Parameters {
//    double deadline = 1200000;
    int num_vms = 3;
    String dax_path = "D:/cloudsim-3.0.3/WorkflowSim/WorkflowSim/dax/1_Montage_4_150.xml";
   
   	BasePlanning planningAlgorithm = new Paper2();
    										// BasePlanning planningAlgorithm = new CPOP();

   // BasePlanning  planningAlgorithm = new GA();

}