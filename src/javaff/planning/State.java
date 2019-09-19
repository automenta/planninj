package javaff.planning;

import javaff.data.Action;
import javaff.data.GroundCondition;
import javaff.data.Plan;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public abstract class State implements Cloneable {
	public GroundCondition goal;

//	public Filter filter = null;

//	public void setFilter(Filter f)
//	{
//		filter = f;
//	}

//	public Filter getFilter()
//	{
//		return filter;
//	}

//	public abstract Set getNextStates();       // get all the next possible states reachable from this state

	public Set getNextStates(Set actions)      // get all the states after applying this set of actions
	{
		Set rSet = new HashSet();
		for (Object action : actions) {
			Action a = (Action) action;
			rSet.add(this.apply(a));
		}
		return rSet;
	}

	public State apply(Action a)    // return a cloned copy
	{
		State s = null;
		try {
			s = (State) this.clone();
		} catch (CloneNotSupportedException e) {
			javaff.JavaFF.errorOutput.println(e);
		}
		a.apply(s);
		return s;
	}

	public abstract BigDecimal getHValue();

	public abstract BigDecimal getGValue();

	public boolean goalReached() {
		return goal.isTrue(this);
	}

	public abstract Plan getSolution();

	public abstract Set getActions();

	public boolean checkAvailability(Action a) //put in for invariant checking
	{
		return true;
	}
}
