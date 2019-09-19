package javaff.search;

import javaff.planning.Filter;
import javaff.planning.State;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;


public class BestFirstSearch extends Search {

	protected final HashMap closed;
	protected final TreeSet open;
	protected Filter filter = null;

	public BestFirstSearch(State s) {
		this(s, new HValueComparator());
	}

	public BestFirstSearch(State s, Comparator c) {
		super(s);
		setComparator(c);

		closed = new HashMap();
		open = new TreeSet(comp);
	}

	public void setFilter(Filter f) {
		filter = f;
	}

	public void updateOpen(State S) {
		open.addAll(S.getNextStates(filter.getActions(S)));
	}

	public State removeNext() {
		State S = (State) open.first();
		open.remove(S);
                /*
                System.out.println("================================");
		S.getSolution().print(System.out);
		System.out.println("----Helpful Actions-------------");
		javaff.planning.TemporalMetricState ms = (javaff.planning.TemporalMetricState) S;
		System.out.println(ms.helpfulActions);
		System.out.println("----Relaxed Plan----------------");
		ms.RelaxedPlan.print(System.out);
                */
		return S;
	}

	public boolean needToVisit(State s) {
		Integer Shash = s.hashCode();
		State D = (State) closed.get(Shash);

		if (closed.containsKey(Shash) && D.equals(s)) return false;

		closed.put(Shash, s);
		return true;
	}

	public State search() {

		open.add(start);

		while (!open.isEmpty()) {
			State s = removeNext();
			if (needToVisit(s)) {
				++nodeCount;
				if (s.goalReached()) {
					return s;
				} else {
					updateOpen(s);
				}
			}

		}
		return null;
	}

}
