package javaff.search;

import javaff.planning.Filter;
import javaff.planning.State;

import java.math.BigDecimal;
import java.util.*;

public class EnforcedHillClimbingSearch extends Search {
	protected BigDecimal bestHValue;

	protected final HashMap closed;
	protected LinkedList open;
	protected Filter filter = null;

	public EnforcedHillClimbingSearch(State s) {
		this(s, new HValueComparator());
	}

	public EnforcedHillClimbingSearch(State s, Comparator c) {
		super(s);
		setComparator(c);

		closed = new HashMap();
		open = new LinkedList();
	}

	public void setFilter(Filter f) {
		filter = f;
	}

	public State removeNext() {

		return (State) open.removeFirst();
	}

	public boolean needToVisit(State s) {
		Integer Shash = s.hashCode(); // compute hash for state
		State D = (State) closed.get(Shash); // see if its on the closed list

		if (closed.containsKey(Shash) && D.equals(s)) return false;  // if it is return false

		closed.put(Shash, s); // otherwise put it on
		return true; // and return true
	}

	public State search() {

		if (start.goalReached()) { // wishful thinking
			return start;
		}

		needToVisit(start); // dummy call (adds start to the list of 'closed' states so we don't visit it again

		open.add(start); // add it to the open list
		bestHValue = start.getHValue(); // and take its heuristic value as the best so far

		javaff.JavaFF.infoOutput.println(bestHValue);

		while (!open.isEmpty()) // whilst still states to consider
		{
			State s = removeNext(); // get the next one

			Set successors = s.getNextStates(filter.getActions(s)); // and find its neighbourhood

			for (Object successor : successors) {
				State succ = (State) successor; // next successor

				if (needToVisit(succ)) {
					if (succ.goalReached()) { // if we've found a goal state - return it as the solution
						return succ;
					} else if (succ.getHValue().compareTo(bestHValue) < 0) {
						// if we've found a state with a better heuristic value than the best seen so far

						bestHValue = succ.getHValue(); // note the new best avlue
						javaff.JavaFF.infoOutput.println(bestHValue);
						open = new LinkedList(); // clear the open list
						open.add(succ); // put this on it
						break; // and skip looking at the other successors
					} else {
						open.add(succ); // otherwise, add to the open list
					}
				}
			}


		}
		return null;
	}
}
