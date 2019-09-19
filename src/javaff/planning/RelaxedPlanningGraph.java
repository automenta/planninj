package javaff.planning;

import javaff.data.GroundProblem;

import java.util.Set;

public class RelaxedPlanningGraph extends PlanningGraph {
	public RelaxedPlanningGraph(GroundProblem gp) {
		super(gp);
	}

	protected boolean checkPropMutex(MutexPair m, int l) {
		return false;
	}

	protected boolean checkPropMutex(PGProposition p1, PGProposition p2, int l) {
		return false;
	}

	protected boolean checkActionMutex(MutexPair m, int l) {
		return false;
	}

	protected boolean checkActionMutex(PGAction a1, PGAction a2, int l) {
		return false;
	}

	protected boolean noMutexes(Set s, int l) {
		return true;
	}

	protected boolean noMutexesTest(Node n, Set s, int l) // Tests to see if there is a mutex between n and all nodes in s
	{
		return true;
	}
}
