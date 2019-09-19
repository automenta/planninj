package javaff.scheduling;

import javaff.data.Action;
import javaff.data.metric.BinaryComparator;
import javaff.data.metric.MetricSymbolStore;
import javaff.data.metric.ResourceOperator;
import javaff.data.strips.InstantAction;
import javaff.data.temporal.DurationFunction;
import javaff.data.temporal.DurativeAction;

import java.math.BigDecimal;
import java.util.*;

//OK for new precedence relations (i.e. meetCosntraints) should move consumers to AFTER the >= etc..) (actually maybe no)
// AND for the new bounds should do incremental sweeps as in precedence relations

public class PrecedenceResourceGraph {
	public final Map operators = new HashMap();
	public final Map conditions = new HashMap();
	public Map states = new HashMap();          // Maps (Operators || Conditions => States)

	public final MatrixSTN stn;

	public PrecedenceResourceGraph(MatrixSTN s) {
		stn = s;
	}

	public void addCondition(BinaryComparator bc, Action a) {
		conditions.put(bc, a);
	}

	public void addOperator(ResourceOperator ro, Action a) {
		operators.put(ro, a);
	}

	public boolean meetConditions() {
		boolean changed = false;
		for (Object o : conditions.keySet()) {
			BinaryComparator bc = (BinaryComparator) o;
			BigDecimal comp = bc.second.getValue(null);
			Action a = (Action) conditions.get(bc);

			if (bc.type == MetricSymbolStore.LESS_THAN || bc.type == MetricSymbolStore.LESS_THAN_EQUAL) {
				BigDecimal value = findBeforeMin(a);

				if (value.compareTo(comp) >= 0) {
					//move an unordered consumer back
					Set u = getUnorderedConsumers(a);
					Action a2 = stn.getEarliest(u);
					stn.addConstraint(TemporalConstraint.getConstraint((InstantAction) a2, (InstantAction) a));
					changed = true;
				}
			} else if (bc.type == MetricSymbolStore.GREATER_THAN || bc.type == MetricSymbolStore.GREATER_THAN_EQUAL) {
				BigDecimal value = findBeforeMax(a);
				if (value.compareTo(comp) <= 0) {
					//move an unordered producer back
					Set u = getUnorderedProducers(a);
					Action a2 = stn.getEarliest(u);
					stn.addConstraint(TemporalConstraint.getConstraint((InstantAction) a2, (InstantAction) a));
					changed = true;
				}
			}
		}
		return changed;
	}

	private BigDecimal findBeforeMax(Action a) {
		BigDecimal value = new BigDecimal(0);
		for (Object o : operators.keySet()) {
			ResourceOperator ro = (ResourceOperator) o;
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.INCREASE || ro.type == MetricSymbolStore.SCALE_UP) {
				if (stn.B(a2, a) || stn.BS(a2, a))
					value = ro.applyMax(value, stn); // WARNING This is not taking into the account the order of the actions

			} else if (ro.type == MetricSymbolStore.DECREASE || ro.type == MetricSymbolStore.SCALE_DOWN) {
				if (stn.B(a2, a)) value = ro.applyMin(value, stn);
			}
		}
		return value;
	}

	private BigDecimal findBeforeMin(Action a) {
		BigDecimal value = new BigDecimal(0);
		for (Object o : operators.keySet()) {
			ResourceOperator ro = (ResourceOperator) o;
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.INCREASE || ro.type == MetricSymbolStore.SCALE_UP) {
				if (stn.B(a2, a))
					value = ro.applyMin(value, stn); // WARNING This is not taking into the account the order of the actions
			} else if (ro.type == MetricSymbolStore.DECREASE || ro.type == MetricSymbolStore.SCALE_DOWN) {
				if (stn.B(a2, a) || stn.BS(a2, a)) value = ro.applyMax(value, stn);
			}
		}
		return value;
	}


	private Set getUnorderedProducers(Action a) {
		Set rSet = new HashSet();
		for (Object o : operators.keySet()) {
			ResourceOperator ro = (ResourceOperator) o;
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.INCREASE || ro.type == MetricSymbolStore.SCALE_UP) {
				if (stn.U(a2, a)) rSet.add(a2);
			}
		}
		return rSet;
	}

	private Set getUnorderedConsumers(Action a) {
		Set rSet = new HashSet();
		for (Object o : operators.keySet()) {
			ResourceOperator ro = (ResourceOperator) o;
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.DECREASE || ro.type == MetricSymbolStore.SCALE_DOWN) {
				if (stn.U(a2, a)) rSet.add(a2);
			}
		}
		return rSet;
	}

	private Set getBeforeOperators(Action a) {
		Set rSet = new HashSet();
		for (Object o : operators.keySet()) {
			ResourceOperator ro = (ResourceOperator) o;
			Action a2 = (Action) operators.get(ro);
			rSet.add(ro);
		}
		return rSet;
	}


	public boolean limitBounds() {
		boolean change = false;
		for (Object item : conditions.keySet()) {
			BinaryComparator bc = (BinaryComparator) item;

			BigDecimal comp = bc.second.getValue(null);
			Action a = (Action) conditions.get(bc);

			if (bc.type == MetricSymbolStore.LESS_THAN || bc.type == MetricSymbolStore.LESS_THAN_EQUAL) {
				BigDecimal value = findBeforeMax(a);
				if (value.compareTo(comp) > 0) {
					BigDecimal diff = value.subtract(comp);
					//change an before producers back
					Set u = getBeforeOperators(a);
					for (Object o : u) {
						ResourceOperator ro = (ResourceOperator) o;
						if (ro.change instanceof DurationFunction) {
							DurationFunction df = (DurationFunction) ro.change;
							DurativeAction da = df.durativeAction;
							if (ro.type == MetricSymbolStore.INCREASE || ro.type == MetricSymbolStore.SCALE_UP)
								stn.decreaseMax(da, diff);
							else if (ro.type == MetricSymbolStore.DECREASE || ro.type == MetricSymbolStore.SCALE_DOWN)
								stn.increaseMin(da, diff);
							change = true;
							break;
						}
					}
				}
			} else if (bc.type == MetricSymbolStore.GREATER_THAN || bc.type == MetricSymbolStore.GREATER_THAN_EQUAL) {
				BigDecimal value = findBeforeMin(a);
				if (value.compareTo(comp) < 0) {
					BigDecimal diff = comp.subtract(value);
					//change an before producers back
					Set u = getBeforeOperators(a);
					for (Object o : u) {
						ResourceOperator ro = (ResourceOperator) o;
						if (ro.change instanceof DurationFunction) {
							DurationFunction df = (DurationFunction) ro.change;
							DurativeAction da = df.durativeAction;
							if (ro.type == MetricSymbolStore.INCREASE || ro.type == MetricSymbolStore.SCALE_UP)
								stn.increaseMin(da, diff);
							else if (ro.type == MetricSymbolStore.DECREASE || ro.type == MetricSymbolStore.SCALE_DOWN)
								stn.decreaseMax(da, diff);
							change = true;
							break;
						}
					}
				}
			}
		}
		return change;
	}

	public void minimize() {
		for (Object o : operators.keySet()) {
			ResourceOperator ro = (ResourceOperator) o;
			if (ro.change instanceof DurationFunction) {
				DurativeAction da = ((DurationFunction) ro.change).durativeAction;
				if (ro.type == MetricSymbolStore.INCREASE || ro.type == MetricSymbolStore.SCALE_UP) {
					stn.minimize(da);
				} else if (ro.type == MetricSymbolStore.DECREASE || ro.type == MetricSymbolStore.SCALE_DOWN) {
					stn.maximize(da);
				}
			}
		}
	}

	public void maximize() {
		for (Object o : operators.keySet()) {
			ResourceOperator ro = (ResourceOperator) o;
			if (ro.change instanceof DurationFunction) {
				DurativeAction da = ((DurationFunction) ro.change).durativeAction;
				if (ro.type == MetricSymbolStore.INCREASE || ro.type == MetricSymbolStore.SCALE_UP) {
					stn.maximize(da);
				} else if (ro.type == MetricSymbolStore.DECREASE || ro.type == MetricSymbolStore.SCALE_DOWN) {
					stn.minimize(da);
				}
			}
		}
	}

}
