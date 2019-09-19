package javaff.scheduling;

import javaff.data.Action;
import javaff.data.GroundProblem;
import javaff.data.PartialOrderPlan;
import javaff.data.TotalOrderPlan;
import javaff.data.strips.Proposition;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

public class GreedyPartialOrderLifter implements PartialOrderLifter {
	//This could probably make use of the data structures in the relaxed plan graph
	public static PartialOrderPlan lift(TotalOrderPlan top, GroundProblem problem) {
		/* WARNING - this code does not work, but should have something in instead
        //Must create new instances for where the same action is performed more than once
        List TOPlan = new ArrayList();
        Iterator pit = pTOPlan.iterator();
        while (pit.hasNext())
        {
            InstantAction a = (InstantAction) pit.next();
            TOPlan.add(a.clone());
        }
		 */

		PartialOrderPlan pop = new PartialOrderPlan();
		pop.addActions(top.getActions());

		Set goals = new HashSet(problem.goal.getConditionalPropositions());

		ListIterator toit = top.listIteratorEnd();
		while (toit.hasPrevious()) {
			Action a = (Action) toit.previous();

			getAOrderings(a, top, pop);
			getBOrderings(a, top, pop);
			getCOrderings(a, top, pop, goals);

			goals.addAll(a.getConditionalPropositions());
		}

		return pop;
	}

	public static void getAOrderings(Action a, TotalOrderPlan top, PartialOrderPlan pop) {
		for (Object o : a.getConditionalPropositions()) {
			Proposition c = (Proposition) o;
			ListIterator bit = top.listIterator(a);
			while (bit.hasPrevious()) {
				Action b = (Action) bit.previous();
				Set achieves = b.getAddPropositions();
				if (achieves.contains(c)) {
					pop.addOrder(b, a, c);
					break;
				}
			}
		}
	}

	public static void getBOrderings(Action a, TotalOrderPlan top, PartialOrderPlan pop) {
		for (Object o : a.getDeletePropositions()) {
			Proposition d = (Proposition) o;
			ListIterator bit = top.listIterator(a);
			while (bit.hasPrevious()) {
				Action b = (Action) bit.previous();
				Set conds = b.getConditionalPropositions();
				if (conds.contains(d)) {
					pop.addOrder(b, a, d);
				}
			}
		}
	}

	public static void getCOrderings(Action a, TotalOrderPlan top, PartialOrderPlan pop, Set goals) {
		Set as = a.getAddPropositions();
		as.retainAll(goals);
		for (Object o : as) {
			Proposition ad = (Proposition) o;
			ListIterator bit = top.listIterator(a);
			while (bit.hasPrevious()) {
				Action b = (Action) bit.previous();
				Set dels = b.getDeletePropositions();
				if (dels.contains(ad)) {
					pop.addOrder(b, a, ad);
				}
			}
		}
	}

}
