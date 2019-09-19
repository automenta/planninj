// checks the schedulability of a forward produced plan

package javaff.scheduling;

import javaff.data.strips.InstantAction;
import javaff.planning.TemporalMetricState;

public interface SchedulabilityChecker {
	Object clone();

	boolean addAction(InstantAction a, TemporalMetricState s);
}
