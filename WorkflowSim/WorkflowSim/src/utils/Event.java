package utils;

import org.workflowsim.Task;
import vm.CustomVM;

public class Event implements Comparable<Event> {
    public double start;
    public double finish;
    public Task task;
    public double runFreq;
    public CustomVM vm;

    public Event(double start, double finish, Task task, double runFreq, CustomVM vm) {
        this.start = start;
        this.finish = finish;
        this.task = task;
        this.runFreq = runFreq;
        this.vm = vm;
    }
  

    public Event(double start, double finish, Task task, CustomVM vm) {
        this(start, finish, task, vm.getMaxFreq(), vm);
    }


    @Override
    public int compareTo(Event rhs) {
        if(this.start > rhs.start)
            return 1;
        else if(rhs.start > this.start)
            return -1;
        else if(rhs.finish > this.finish)
            return -1;
        else if(rhs.finish < this.finish)
            return 1;
        return 0;
    }

    @Override
    public String toString() {
        return "Event(" + start + " to " + finish + ")";
    }
}
