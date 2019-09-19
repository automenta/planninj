package javaff.data;

import javaff.data.strips.InstantAction;
import javaff.data.temporal.DurativeAction;
import javaff.planning.*;

import java.util.*;

public class GroundProblem {
	//public Set facts = new HashSet();                  // (Proposition)
	public Set actions = new HashSet();                // (GroundAction)
	public Map functionValues = new HashMap();     // (NamedFunction => BigDecimal)
	public final Metric metric;

	public final GroundCondition goal;
	public final Set initial;                                // (Proposition)

	public TemporalMetricState state = null;

	public GroundProblem(Set a, Set i, GroundCondition g, Map f, Metric m) {
		actions = a;
		initial = i;
		goal = g;
		functionValues = f;
		metric = m;
	}

	public STRIPSState getSTRIPSInitialState() {
		STRIPSState s = new STRIPSState(actions, initial, goal);
		s.setRPG(new RelaxedPlanningGraph(this));
		return s;
	}

	public MetricState getMetricInitialState() {
		MetricState ms = new MetricState(actions, initial, goal, functionValues, metric);
		ms.setRPG(new RelaxedMetricPlanningGraph(this));
		return ms;
	}

	public TemporalMetricState getTemporalMetricInitialState() {
		if (state == null) {
			Set na = new HashSet();
			Set ni = new HashSet();
			for (Object action : actions) {
				Action act = (Action) action;
				if (act instanceof InstantAction) {
					na.add(act);
					ni.add(act);
				} else if (act instanceof DurativeAction) {
					DurativeAction dact = (DurativeAction) act;
					na.add(dact.startAction);
					na.add(dact.endAction);
					ni.add(dact.startAction);
				}
			}
			TemporalMetricState ts = new TemporalMetricState(ni, initial, goal, functionValues, metric);
			GroundProblem gp = new GroundProblem(na, initial, goal, functionValues, metric);
			ts.setRPG(new RelaxedTemporalMetricPlanningGraph(gp));
			state = ts;
		}
		return state;
	}


}
