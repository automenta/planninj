// WARNING - This State does deal with Metric Invariants

package javaff.planning;

import javaff.data.Action;
import javaff.data.GroundCondition;
import javaff.data.Metric;
import javaff.data.TotalOrderPlan;
import javaff.data.strips.InstantAction;
import javaff.data.temporal.SplitInstantAction;
import javaff.data.temporal.StartInstantAction;
import javaff.scheduling.SchedulabilityChecker;
import javaff.scheduling.VelosoSchedulabilityChecker;

import java.util.*;
//import java.math.BigDecimal;

public class TemporalMetricState extends MetricState {
	public final Set openActions;         //Set of (DurativeActions)
	public final List invariants;            //Set of Propositions (Propositions)
	public SchedulabilityChecker checker;

	protected TemporalMetricState(Set a, Set f, GroundCondition g, Map funcs, TotalOrderPlan p, Metric m, Set oAc, List i) {
		super(a, f, g, funcs, p, m);
		openActions = oAc;
		invariants = i;
	}

	public TemporalMetricState(Set a, Set f, GroundCondition g, Map funcs, Metric m) {
		super(a, f, g, funcs, m);
		openActions = new HashSet();
		invariants = new ArrayList();
		checker = new VelosoSchedulabilityChecker();

	}

	public boolean goalReached() {
		return (openActions.isEmpty() && super.goalReached());
	}

	public boolean checkAvailability(Action a) {
		List rList = new ArrayList(invariants);
		if (a instanceof SplitInstantAction) {
			SplitInstantAction da = (SplitInstantAction) a;
			for (Object o : da.parent.invariant.getConditionalPropositions()) {
				rList.remove(o);
			}
		}
		rList.retainAll(a.getDeletePropositions());
		if (!rList.isEmpty()) return false;
		SchedulabilityChecker c = (VelosoSchedulabilityChecker) checker.clone(); //This should have to be cloned here and below
		boolean result = c.addAction((InstantAction) a, this);
		if (result && a instanceof StartInstantAction) {
			TemporalMetricState dupli = (TemporalMetricState) this.clone();
			dupli.apply(a);
			result = c.addAction(((StartInstantAction) a).getSibling(), dupli);

		}
		return result;
	}

	public Object clone() {
		Set nf = (Set) ((HashSet) facts).clone();
		TotalOrderPlan p = (TotalOrderPlan) plan.clone();
		Map nfuncs = (Map) ((HashMap) funcValues).clone();
		Set oA = (Set) ((HashSet) openActions).clone();
		List i = (List) ((ArrayList) invariants).clone();
		Set na = (Set) ((HashSet) actions).clone();
		TemporalMetricState ts = new TemporalMetricState(na, nf, goal, nfuncs, p, metric, oA, i);
		ts.setRPG(RPG);
//		ts.setFilter(filter);
		ts.checker = (VelosoSchedulabilityChecker) checker.clone();
		return ts;
	}

	public State apply(Action a)    // return a cloned copy
	{
		TemporalMetricState s = (TemporalMetricState) super.apply(a);
		if (a instanceof SplitInstantAction) {
			SplitInstantAction sia = (SplitInstantAction) a;
			sia.applySplit(s);
		}
		s.checker.addAction((InstantAction) a, s);
		return s;
	}

	//public BigDecimal getGValue()
	//{
	//return super.getGValue().subtract(new BigDecimal(openActions.size()));
	//}
}
