package javaff.planning;

import javaff.data.*;
import javaff.data.strips.Proposition;

import java.util.*;

public class PlanningGraph {
	static final int NUMERIC_LIMIT = 4;
	protected Set readyActions = null; // PGActions that have all their propositions met, but not their PGBinaryComparators or preconditions are mutex
	//******************************************************
	// Data Structures
	//******************************************************
	final Map propositionMap = new HashMap();      // (Proposition => PGProposition)
	final Map actionMap = new HashMap();           // (Action => PGAction)
	final Set propositions = new HashSet();
	final Set actions = new HashSet();
	Set initial, goal;
	Set propMutexes, actionMutexes;
	List memorised;
	boolean level_off = false;
	int numeric_level_off = 0;
	int num_layers;

	//******************************************************
	// Main methods
	//******************************************************
	protected PlanningGraph() {

	}

	public PlanningGraph(GroundProblem gp) {
		setActionMap(gp.actions);
		setLinks();
		createNoOps();
		setGoal(gp.goal);
	}

	public Plan getPlan(State s) {
		setInitial(s);
		resetAll(s);

		//set up the intital set of facts
		Set scheduledFacts = new HashSet(initial);
		List scheduledActs = null;

		scheduledActs = createFactLayer(scheduledFacts, 0);
		List plan = null;

		//create the graph==========================================
		while (true) {
			scheduledFacts = createActionLayer(scheduledActs, num_layers);
			++num_layers;
			scheduledActs = createFactLayer(scheduledFacts, num_layers);

			if (goalMet() && !goalMutex()) {
				plan = extractPlan();
			}
			if (plan != null) break;
			if (!level_off) numeric_level_off = 0;
			if (level_off || numeric_level_off >= NUMERIC_LIMIT) {
				//printGraph();
				break;
			}
		}


		if (plan != null) {
			Iterator pit = plan.iterator();
			TotalOrderPlan p = new TotalOrderPlan();
			while (pit.hasNext()) {
				PGAction a = (PGAction) pit.next();
				if (!(a instanceof PGNoOp)) p.addAction(a.action);
			}
			//p.print(javaff.JavaFF.infoOutput);
			return p;
		} else return null;

	}

	//******************************************************
	// Setting it all up
	//******************************************************
	protected void setActionMap(Set gactions) {
		for (Object gaction : gactions) {
			Action a = (Action) gaction;
			PGAction pga = new PGAction(a);
			actionMap.put(a, pga);
			actions.add(pga);
		}
	}

	protected PGProposition getProposition(Proposition p) {
		Object o = propositionMap.get(p);
		PGProposition pgp;
		if (o == null) {
			pgp = new PGProposition(p);
			propositionMap.put(p, pgp);
			propositions.add(pgp);
		} else pgp = (PGProposition) o;
		return pgp;
	}

	protected void setLinks() {
		for (Object action : actions) {
			PGAction pga = (PGAction) action;

			for (Object item : pga.action.getConditionalPropositions()) {
				Proposition p = (Proposition) item;
				PGProposition pgp = getProposition(p);
				pga.conditions.add(pgp);
				pgp.achieves.add(pga);
			}

			for (Object value : pga.action.getAddPropositions()) {
				Proposition p = (Proposition) value;
				PGProposition pgp = getProposition(p);
				pga.achieves.add(pgp);
				pgp.achievedBy.add(pga);
			}

			for (Object o : pga.action.getDeletePropositions()) {
				Proposition p = (Proposition) o;
				PGProposition pgp = getProposition(p);
				pga.deletes.add(pgp);
				pgp.deletedBy.add(pga);
			}
		}
	}

	protected void resetAll(State s) {
		propMutexes = new HashSet();
		actionMutexes = new HashSet();

		memorised = new ArrayList();

		readyActions = new HashSet();

		num_layers = 0;

		for (Object action : actions) {
			PGAction a = (PGAction) action;
			a.reset();
		}

		for (Object proposition : propositions) {
			PGProposition p = (PGProposition) proposition;
			p.reset();
		}
	}

	protected void setGoal(GroundCondition g) {
		goal = new HashSet();
		for (Object o : g.getConditionalPropositions()) {
			Proposition p = (Proposition) o;
			PGProposition pgp = getProposition(p);
			goal.add(pgp);
		}
	}

	protected void setInitial(State S) {
		Set i = ((STRIPSState) S).facts;
		initial = new HashSet();
		for (Object o : i) {
			Proposition p = (Proposition) o;
			PGProposition pgp = getProposition(p);
			initial.add(pgp);
		}
	}

	protected void createNoOps() {
		for (Object proposition : propositions) {
			PGProposition p = (PGProposition) proposition;
			PGNoOp n = new PGNoOp(p);
			n.conditions.add(p);
			n.achieves.add(p);
			p.achieves.add(n);
			p.achievedBy.add(n);
			actions.add(n);
		}
	}

	//******************************************************
	// Graph Construction
	//******************************************************

	protected ArrayList createFactLayer(Set pFacts, int pLayer) {
		memorised.add(new HashSet());
		ArrayList scheduledActs = new ArrayList();
		HashSet newMutexes = new HashSet();
		for (Object pFact : pFacts) {
			PGProposition f = (PGProposition) pFact;
			if (f.layer < 0) {
				f.layer = pLayer;
				scheduledActs.addAll(f.achieves);
				level_off = false;

				//calculate mutexes
				if (pLayer != 0) {
					for (Object proposition : propositions) {
						PGProposition p = (PGProposition) proposition;
						if (p.layer >= 0 && checkPropMutex(f, p, pLayer)) {
							makeMutex(f, p, pLayer, newMutexes);
						}
					}
				}

			}
		}

		//check old mutexes
		for (Object propMutex : propMutexes) {
			MutexPair m = (MutexPair) propMutex;
			if (checkPropMutex(m, pLayer)) {
				makeMutex(m.node1, m.node2, pLayer, newMutexes);
			} else {
				level_off = false;
			}
		}

		//add new mutexes to old mutexes and remove those which have disappeared
		propMutexes = newMutexes;


		return scheduledActs;
	}

	protected boolean checkPropMutex(MutexPair m, int l) {
		return checkPropMutex((PGProposition) m.node1, (PGProposition) m.node2, l);
	}

	protected boolean checkPropMutex(PGProposition p1, PGProposition p2, int l) {
		if (p1 == p2) return false;

		//Componsate for statics
		if (p1.achievedBy.isEmpty() || p2.achievedBy.isEmpty()) return false;

		for (Object value : p1.achievedBy) {
			PGAction a1 = (PGAction) value;
			if (a1.layer >= 0) {
				for (Object o : p2.achievedBy) {
					PGAction a2 = (PGAction) o;
					if (a2.layer >= 0 && !a1.mutexWith(a2, l - 1)) return false;
				}
			}

		}
		return true;
	}

	protected void makeMutex(Node n1, Node n2, int l, Set mutexPairs) {
		n1.setMutex(n2, l);
		n2.setMutex(n1, l);
		mutexPairs.add(new MutexPair(n1, n2));
	}

	protected HashSet createActionLayer(List pActions, int pLayer) {
		level_off = true;
		HashSet actionSet = getAvailableActions(pActions, pLayer);
		actionSet.addAll(readyActions);
		readyActions = new HashSet();
		HashSet filteredSet = filterSet(actionSet, pLayer);
		HashSet scheduledFacts = calculateActionMutexesAndProps(filteredSet, pLayer);
		return scheduledFacts;
	}

	protected HashSet getAvailableActions(List pActions, int pLayer) {
		HashSet actionSet = new HashSet();
		for (Object pAction : pActions) {
			PGAction a = (PGAction) pAction;
			if (a.layer < 0) {
				a.counter++;
				a.difficulty += pLayer;
				if (a.counter >= a.conditions.size()) {
					actionSet.add(a);
					level_off = false;
				}
			}
		}
		return actionSet;
	}

	protected HashSet filterSet(Set pActions, int pLayer) {
		HashSet filteredSet = new HashSet();
		for (Object pAction : pActions) {
			PGAction a = (PGAction) pAction;
			if (noMutexes(a.conditions, pLayer))
				filteredSet.add(a);
			else
				readyActions.add(a);
		}
		return filteredSet;
	}

	protected HashSet calculateActionMutexesAndProps(Set filteredSet, int pLayer) {
		HashSet newMutexes = new HashSet();

		Set newPreActions = new HashSet();
		HashSet scheduledFacts = new HashSet();

		for (Object o : filteredSet) {
			PGAction a = (PGAction) o;
			scheduledFacts.addAll(a.achieves);
			a.layer = pLayer;
			level_off = false;

			//caculate new mutexes
			for (Object action : actions) {
				PGAction a2 = (PGAction) action;
				if (a2.layer >= 0 && checkActionMutex(a, a2, pLayer)) {
					makeMutex(a, a2, pLayer, newMutexes);
				}
			}
		}

		//check old mutexes
		for (Object actionMutex : actionMutexes) {
			MutexPair m = (MutexPair) actionMutex;
			if (checkActionMutex(m, pLayer)) {
				makeMutex(m.node1, m.node2, pLayer, newMutexes);
			} else {
				level_off = false;
			}
		}

		//add new mutexes to old mutexes and remove those which have disappeared
		actionMutexes = newMutexes;
		return scheduledFacts;
	}

	protected boolean checkActionMutex(MutexPair m, int l) {
		return checkActionMutex((PGAction) m.node1, (PGAction) m.node2, l);
	}

	protected boolean checkActionMutex(PGAction a1, PGAction a2, int l) {
		if (a1 == a2) return false;

		for (Object element : a1.deletes) {
			PGProposition p1 = (PGProposition) element;
			if (a2.achieves.contains(p1)) return true;
			if (a2.conditions.contains(p1)) return true;
		}

		for (Object item : a2.deletes) {
			PGProposition p2 = (PGProposition) item;
			if (a1.achieves.contains(p2)) return true;
			if (a1.conditions.contains(p2)) return true;
		}

		for (Object value : a1.conditions) {
			PGProposition p1 = (PGProposition) value;
			for (Object o : a2.conditions) {
				PGProposition p2 = (PGProposition) o;
				if (p1.mutexWith(p2, l)) return true;
			}
		}

		return false;
	}


	protected boolean goalMet() {
		for (Object o : goal) {
			PGProposition p = (PGProposition) o;
			if (p.layer < 0) return false;
		}
		return true;
	}

	protected boolean goalMutex() {
		return !noMutexes(goal, num_layers);
	}

	protected boolean noMutexes(Set s, int l) {
		Iterator sit = s.iterator();
		if (sit.hasNext()) {
			Node n = (Node) sit.next();
			HashSet s2 = new HashSet(s);
			s2.remove(n);
			for (Object o : s2) {
				Node n2 = (Node) o;
				if (n.mutexWith(n2, l)) return false;
			}
			return noMutexes(s2, l);
		} else return true;
	}

	protected boolean noMutexesTest(Node n, Set s, int l) // Tests to see if there is a mutex between n and all nodes in s
	{
		for (Object o : s) {
			Node n2 = (Node) o;
			if (n.mutexWith(n2, l)) return false;
		}
		return true;
	}


	//******************************************************
	// Plan Extraction
	//******************************************************


	public List extractPlan() {
		return searchPlan(goal, num_layers);
	}

	public List searchPlan(Set goalSet, int l) {

		if (l == 0) {
			if (initial.containsAll(goalSet)) return new ArrayList();
			else return null;
		}
		// do memorisation stuff
		Set badGoalSet = (HashSet) memorised.get(l);
		if (badGoalSet.contains(goalSet)) return null;

		List ass = searchLevel(goalSet, (l - 1)); // returns a set of sets of possible action combinations

		for (Object value : ass) {
			Set as = (HashSet) value;
			Set newgoal = new HashSet();

			for (Object o : as) {
				PGAction a = (PGAction) o;
				newgoal.addAll(a.conditions);
			}

			List al = searchPlan(newgoal, (l - 1));
			if (al != null) {
				List plan = new ArrayList(al);
				plan.addAll(as);
				return plan;
			}

		}

		// do more memorisation stuff
		badGoalSet.add(goalSet);
		return null;


	}


	public List searchLevel(Set goalSet, int layer) {
		if (goalSet.isEmpty()) {
			Set s = new HashSet();
			List li = new ArrayList();
			li.add(s);
			return li;
		}

		List actionSetList = new ArrayList();
		Set newGoalSet = new HashSet(goalSet);

		Iterator git = goalSet.iterator();
		PGProposition g = (PGProposition) git.next();
		newGoalSet.remove(g);

		Iterator ait = g.achievedBy.iterator();
		while (ait.hasNext()) {
			PGAction a = (PGAction) ait.next();
			if ((a instanceof PGNoOp) && a.layer <= layer && a.layer >= 0) {
				Set newnewGoalSet = new HashSet(newGoalSet);
				newnewGoalSet.removeAll(a.achieves);
				List l = searchLevel(newnewGoalSet, layer);
				for (Object o : l) {
					Set s = (HashSet) o;
					if (noMutexesTest(a, s, layer)) {
						s.add(a);
						actionSetList.add(s);
					}
				}
			}
		}

		ait = g.achievedBy.iterator();
		while (ait.hasNext()) {
			PGAction a = (PGAction) ait.next();
			if (!(a instanceof PGNoOp) && a.layer <= layer && a.layer >= 0) {
				Set newnewGoalSet = new HashSet(newGoalSet);
				newnewGoalSet.removeAll(a.achieves);
				List l = searchLevel(newnewGoalSet, layer);
				for (Object o : l) {
					Set s = (HashSet) o;
					if (noMutexesTest(a, s, layer)) {
						s.add(a);
						actionSetList.add(s);
					}
				}
			}
		}


		return actionSetList;
	}

	//******************************************************
	// Useful Methods
	//******************************************************

	public int getLayer(Action a) {
		PGAction pg = (PGAction) actionMap.get(a);
		return pg.layer;
	}

	//******************************************************
	// Debugging Classes
	//******************************************************
	public void printGraph() {
		for (int i = 0; i <= num_layers; ++i) {
			System.out.println("-----Layer " + i + "----------------------------------------");
			printLayer(i);
		}
		System.out.println("-----End -----------------------------------------------");
	}

	public void printLayer(int i) {
		System.out.println("Facts:");
		for (Object proposition : propositions) {
			PGProposition p = (PGProposition) proposition;
			if (p.layer <= i && p.layer >= 0) {
				System.out.println("\t" + p);
				System.out.println("\t\tmutex with");
				for (Object o : p.mutexTable.keySet()) {
					PGProposition pm = (PGProposition) o;
					Integer il = (Integer) p.mutexTable.get(pm);
					if (il >= i) {
						System.out.println("\t\t\t" + pm);
					}
				}
			}
		}
		if (i == num_layers) return;
		System.out.println("Actions:");
		for (Object action : actions) {
			PGAction a = (PGAction) action;
			if (a.layer <= i && a.layer >= 0) {
				System.out.println("\t" + a);
				System.out.println("\t\tmutex with");
				for (Object o : a.mutexTable.keySet()) {
					PGAction am = (PGAction) o;
					Integer il = (Integer) a.mutexTable.get(am);
					if (il >= i) {
						System.out.println("\t\t\t" + am);
					}
				}
			}
		}
	}

	//******************************************************
	// protected Classes
	//******************************************************
	protected static class Node {
		public int layer;
		public Set mutexes;

		public Map mutexTable;

		public Node() {
		}

		public void reset() {
			layer = -1;
			mutexes = new HashSet();
			mutexTable = new HashMap();
		}

		public void setMutex(Node n, int l) {
			n.mutexTable.put(this, l);
			this.mutexTable.put(n, l);
		}

		public boolean mutexWith(Node n, int l) {
			/*
			 if (this == n) return false;
			 Iterator mit = mutexes.iterator();
			 while (mit.hasNext())
			 {
				 Mutex m = (Mutex) mit.next();
				 if (m.contains(n))
				 {
					 return m.layer >= l;
				 }
			 }
			 return false;
			 */
			Object o = mutexTable.get(n);
			if (o == null) return false;
			Integer i = (Integer) o;
			return i >= l;
		}
	}

	protected static class PGAction extends Node {
		public Action action;
		public int counter, difficulty;

		public final Set conditions = new HashSet();
		public final Set achieves = new HashSet();
		public final Set deletes = new HashSet();

		public PGAction() {

		}

		public PGAction(Action a) {
			action = a;
		}

		public Set getComparators() {
			return action.getComparators();
		}

		public Set getOperators() {
			return action.getOperators();
		}

		public void reset() {
			super.reset();
			counter = 0;
			difficulty = 0;
		}

		public String toString() {
			return action.toString();
		}
	}

	protected static class PGNoOp extends PGAction {
		public final PGProposition proposition;

		public PGNoOp(PGProposition p) {
			proposition = p;
		}

		public String toString() {
			return ("No-Op " + proposition);
		}

		public Set getComparators() {
			return new HashSet();
		}

		public Set getOperators() {
			return new HashSet();
		}
	}

	protected static class PGProposition extends Node {
		public final Proposition proposition;

		public final Set achieves = new HashSet();
		public final Set achievedBy = new HashSet();
		public final Set deletedBy = new HashSet();

		public PGProposition(Proposition p) {
			proposition = p;
		}

		public String toString() {
			return proposition.toString();
		}
	}

	protected static class MutexPair {
		public final Node node1;
		public final Node node2;

		public MutexPair(Node n1, Node n2) {
			node1 = n1;
			node2 = n2;
		}
	}

}