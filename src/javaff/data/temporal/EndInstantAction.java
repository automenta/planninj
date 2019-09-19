package javaff.data.temporal;

import javaff.data.strips.Proposition;
import javaff.planning.TemporalMetricState;

import java.util.Set;

public class EndInstantAction extends SplitInstantAction {

	public SplitInstantAction getSibling() {
		return parent.startAction;
	}

	public void applySplit(TemporalMetricState ts) {
		Set is = parent.invariant.getConditionalPropositions();

		for (Object i : is) {
			ts.invariants.remove(i);
		}
		ts.openActions.remove(parent);
		ts.actions.remove(this);
		ts.actions.add(getSibling());
	}

	public boolean exclusivelyInvariant(Proposition p) {
		return !parent.endCondition.getConditionalPropositions().contains(p) || !parent.endEffect.getAddPropositions().contains(p) || !parent.endEffect.getDeletePropositions().contains(p);
	}
}
