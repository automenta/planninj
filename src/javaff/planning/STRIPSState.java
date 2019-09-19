package javaff.planning;

import javaff.data.Action;
import javaff.data.GroundCondition;
import javaff.data.Plan;
import javaff.data.TotalOrderPlan;
import javaff.data.strips.Proposition;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class STRIPSState extends State implements Cloneable {
	public Set facts;
	public Set actions;
	public TotalOrderPlan RelaxedPlan = null;
	public Set helpfulActions = null;
	protected TotalOrderPlan plan = new TotalOrderPlan();
	protected RelaxedPlanningGraph RPG;
	protected boolean RPCalculated = false;
	protected BigDecimal HValue = null;

	protected STRIPSState() {

	}

	public STRIPSState(Set a, Set f, GroundCondition g) {
		facts = f;
		goal = g;
		actions = a;
//		filter = NullFilter.getInstance();
	}

	protected STRIPSState(Set a, Set f, GroundCondition g, TotalOrderPlan p) {
		this(a, f, g);
		plan = p;
	}

	public Object clone() {
		Set nf = (Set) ((HashSet) facts).clone();
		TotalOrderPlan p = (TotalOrderPlan) plan.clone();
		STRIPSState SS = new STRIPSState(actions, nf, goal, p);
		SS.setRPG(RPG);
//		SS.setFilter(filter);
		return SS;
	}

	public RelaxedPlanningGraph getRPG() {
		return RPG;
	}

	public void setRPG(RelaxedPlanningGraph rpg) {
		RPG = rpg;
	}

//	public Set getNextStates()     // get all the next possible states reachable from this state
//	{
//		return getNextStates(filter.getActions(this));
//	}

	public State apply(Action a) {
		STRIPSState s = (STRIPSState) super.apply(a);
		s.plan.addAction(a);
		return s;
	}

	public void addProposition(Proposition p) {
		facts.add(p);
	}

	public void removeProposition(Proposition p) {
		facts.remove(p);
	}

	public boolean isTrue(Proposition p) {
		return facts.contains(p);
	}

	public Set getActions() {
		return actions;
	}

	public void calculateRP() {
		if (!RPCalculated) {
			RelaxedPlan = (TotalOrderPlan) RPG.getPlan(this);
			helpfulActions = new HashSet();
			if (!(RelaxedPlan == null)) {
				HValue = new BigDecimal(RelaxedPlan.getPlanLength());

				Iterator it = RelaxedPlan.iterator();
				while (it.hasNext()) {
					Action a = (Action) it.next();
					if (RPG.getLayer(a) == 0) helpfulActions.add(a);
				}
			} else HValue = javaff.JavaFF.MAX_DURATION;
			RPCalculated = true;
		}
	}

	public BigDecimal getHValue() {
		calculateRP();
		return HValue;
	}

	public BigDecimal getGValue() {
		return new BigDecimal(plan.getPlanLength());
	}

	public Plan getSolution() {
		return plan;
	}

	public boolean equals(Object obj) {
		if (obj instanceof STRIPSState) {
			STRIPSState s = (STRIPSState) obj;
			return s.facts.equals(facts);
		} else return false;
	}

	public int hashCode() {
		int hash = 7;
		hash = 31 * hash ^ facts.hashCode();
		return hash;
	}


}
