package javaff.planning;

import javaff.data.Action;
import javaff.data.GroundProblem;
import javaff.data.Plan;
import javaff.data.TotalOrderPlan;
import javaff.data.metric.Function;
import javaff.data.strips.Proposition;
import javaff.data.temporal.DurationFunction;
import javaff.data.temporal.DurativeAction;
import javaff.data.temporal.StartInstantAction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class RelaxedTemporalMetricPlanningGraph extends RelaxedMetricPlanningGraph {
	public RelaxedTemporalMetricPlanningGraph(GroundProblem gp) {
		super(gp);
	}

	protected void resetAll(State S) {
		super.resetAll(S);
		super.setGoal(S.goal);
		addEndActionGoals((TemporalMetricState) S);
	}

	protected void addEndActionGoals(TemporalMetricState S) {
		for (Object o : S.openActions) {
			DurativeAction da = (DurativeAction) o;
			Proposition p = da.dummyGoal;
			goal.add(getProposition(p));
		}
	}

	public Plan getPlan(State s)// if on the start action is in also add the end action as a matter of course
	{
		Plan p = super.getPlan(s);
		if (p != null) {
			Set acts = p.getActions();
			for (Object act : acts) {
				Action a = (Action) act;
				if (a instanceof StartInstantAction) {
					StartInstantAction sa = (StartInstantAction) a;
					if (!acts.contains(sa.getSibling())) ((TotalOrderPlan) p).addAction(sa.getSibling());
				}
			}
		}

		return p;
	}

	protected PGFunction makeFunction(Function f) {
		PGFunction pgf = super.makeFunction(f);
		if (pgf == null && f instanceof DurationFunction)
			pgf = new PGDurationFunction(((DurationFunction) f).durativeAction);
		return pgf;
	}

	protected class PGDurationFunction extends PGNamedFunction {
		public final DurativeAction durAct;

		public PGDurationFunction(DurativeAction da) {
			durAct = da;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins) {
			return durAct.durationConstraint.getMaxDuration(null);
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins) {
			return durAct.durationConstraint.getMinDuration(null);
		}

		public int hashcode() {
			return durAct.hashCode();
		}

		public boolean effectedBy(PGResourceOperator ro) {
			return false;
		}

		public boolean increase(PGResourceOperator ro) {
			return false;
		}

		public boolean decrease(PGResourceOperator ro) {
			return false;
		}
	}
}
