package javaff.search;

import javaff.planning.Filter;
import javaff.planning.State;

import java.util.HashMap;
import java.util.LinkedList;

public class BreadthFirstSearch extends Search {

	protected final LinkedList open;
	protected final HashMap closed;
	protected Filter filter = null;

	public BreadthFirstSearch(State s) {
		super(s);
		open = new LinkedList();
		closed = new HashMap();
	}

	public void setFilter(Filter f) {
		filter = f;
	}


	public void updateOpen(State S) {
		open.addAll(S.getNextStates(filter.getActions(S)));
	}

	public State removeNext() {
		return (State) open.removeFirst();
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
