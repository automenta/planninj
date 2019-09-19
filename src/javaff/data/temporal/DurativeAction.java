package javaff.data.temporal;

import javaff.data.Action;
import javaff.data.GroundCondition;
import javaff.data.GroundEffect;
import javaff.data.strips.Proposition;
import javaff.planning.MetricState;
import javaff.planning.State;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class DurativeAction extends Action {
	public DurationFunction duration;

	public DurationConstraint durationConstraint;

	public GroundCondition startCondition;
	public GroundCondition endCondition;
	public GroundCondition invariant;

	public GroundEffect startEffect;
	public GroundEffect endEffect;

	public SplitInstantAction startAction;
	public SplitInstantAction endAction;

	public Proposition dummyJoin;
	public Proposition dummyGoal;

	public DurativeAction() {
		duration = new DurationFunction(this);
	}

	public boolean staticDuration() {
		return durationConstraint.staticDuration();
	}


	public BigDecimal getDuration(MetricState ms) {
		return durationConstraint.getDuration(ms);
	}

	public BigDecimal getMaxDuration(MetricState ms) {
		return durationConstraint.getMaxDuration(ms);
	}

	public BigDecimal getMinDuration(MetricState ms) {
		return durationConstraint.getMinDuration(ms);
	}

	//WARNING these methods may not work correctly. Only the instant actions should be worked with
	public boolean isApplicable(State s) {
		return startAction.isApplicable(s);
	}

	public void apply(State s) {
		startAction.apply(s);
		endAction.apply(s);
	}

	public Set getConditionalPropositions() {
		Set rSet = startAction.getConditionalPropositions();
		rSet.addAll(endAction.getConditionalPropositions());
		return rSet;
	}

	public Set getAddPropositions() {
		Set rSet = startAction.getAddPropositions();
		rSet.addAll(endAction.getAddPropositions());
		return rSet;
	}

	public Set getDeletePropositions() {
		Set rSet = startAction.getDeletePropositions();
		rSet.addAll(endAction.getDeletePropositions());
		return rSet;
	}

	public Set getComparators() {
		Set rSet = startAction.getComparators();
		rSet.addAll(endAction.getComparators());
		return rSet;
	}

	public Set getOperators() {
		Set rSet = startAction.getOperators();
		rSet.addAll(endAction.getOperators());
		return rSet;
	}

	public void staticify(Map fValues) {
		startCondition = startCondition.staticifyCondition(fValues);
		startEffect = startEffect.staticifyEffect(fValues);
		invariant = invariant.staticifyCondition(fValues);
		endCondition = endCondition.staticifyCondition(fValues);
		endEffect = endEffect.staticifyEffect(fValues);

		startAction.staticify(fValues);
		endAction.staticify(fValues);
	}


}
