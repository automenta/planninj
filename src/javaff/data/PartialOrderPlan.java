package javaff.data;

import javaff.data.strips.InstantAction;
import javaff.data.strips.Proposition;
import javaff.data.temporal.SplitInstantAction;
import javaff.scheduling.TemporalConstraint;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;


public class PartialOrderPlan implements Plan {
	public final Map strictOrderings = new HashMap();
	public final Map equalOrderings = new HashMap();
	public final Set actions = new HashSet();

	public PartialOrderPlan() {

	}

	public void addStrictOrdering(Action first, Action second) {
		Set ord = null;
		Object o = strictOrderings.get(first);
		if (o == null) {
			ord = new HashSet();
			strictOrderings.put(first, ord);
		} else ord = (HashSet) o;
		ord.add(second);
		actions.add(first);
		actions.add(second);
	}

	public void addEqualOrdering(Action first, Action second) {
		Set ord = null;
		Object o = equalOrderings.get(first);
		if (o == null) {
			ord = new HashSet();
			equalOrderings.put(first, ord);
		} else ord = (HashSet) o;
		ord.add(second);
		actions.add(first);
		actions.add(second);
	}

	public void addOrder(Action first, Action second, Proposition p) {
		if (first instanceof SplitInstantAction) {
			SplitInstantAction sa = (SplitInstantAction) first;
			if (!sa.exclusivelyInvariant(p)) {
				addEqualOrdering(first, second);
				return;
			}

		}

		if (second instanceof SplitInstantAction) {
			SplitInstantAction sa = (SplitInstantAction) second;
			if (!sa.exclusivelyInvariant(p)) {
				addEqualOrdering(first, second);
				return;
			}
		}

		addStrictOrdering(first, second);


	}

	public void addAction(Action a) {
		actions.add(a);
		strictOrderings.put(a, new HashSet());
		equalOrderings.put(a, new HashSet());
	}

	public void addActions(Set s) {
		for (Object o : s) addAction((Action) o);
	}

	public Set getActions() {
		return actions;
	}

	public Set getTemporalConstraints() {
		Set rSet = new HashSet();
		for (Object action : actions) {
			Action a = (Action) action;

			Set ss = (HashSet) strictOrderings.get(a);
			for (Object s : ss) {
				Action b = (Action) s;
				rSet.add(TemporalConstraint.getConstraint((InstantAction) a, (InstantAction) b));
			}

			Set es = (HashSet) equalOrderings.get(a);
			for (Object e : es) {
				Action b = (Action) e;
				rSet.add(TemporalConstraint.getConstraintEqual((InstantAction) a, (InstantAction) b));
			}
		}
		return rSet;

	}

	public void print(PrintStream p) {
		for (Object action : actions) {
			Action a = (Action) action;
			p.println(a);
			p.println("\tStrict Orderings: " + strictOrderings.get(a));
			p.println("\tLess than or equal Orderings: " + equalOrderings.get(a));
		}
	}

	public void print(PrintWriter p) {
		for (Object action : actions) {
			Action a = (Action) action;
			p.println(a);
			p.println("\tStrict Orderings: " + strictOrderings.get(a));
			p.println("\tLess than or equal Orderings: " + equalOrderings.get(a));
		}
	}
}
