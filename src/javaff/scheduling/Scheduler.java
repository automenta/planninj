package javaff.scheduling;

import javaff.data.TimeStampedPlan;
import javaff.data.TotalOrderPlan;

public interface Scheduler {
	TimeStampedPlan schedule(TotalOrderPlan top);
}
