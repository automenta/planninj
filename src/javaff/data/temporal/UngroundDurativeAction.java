package javaff.data.temporal;

import javaff.data.Action;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundCondition;
import javaff.data.UngroundEffect;
import javaff.data.strips.*;

import java.util.Map;
import java.util.Set;

public class UngroundDurativeAction extends Operator {
	public final DurationFunction duration;

	public DurationConstraint durationConstraint;

	public final UngroundCondition startCondition = new AND();
	public final UngroundCondition endCondition = new AND();
	public final UngroundCondition invariant = new AND();

	public final UngroundEffect startEffect = new AND();
	public final UngroundEffect endEffect = new AND();

	public UngroundInstantAction startAction;
	public UngroundInstantAction endAction;

	public Predicate dummyJoin;
	public Predicate dummyGoal;

	public UngroundDurativeAction() {
		duration = new DurationFunction(this);
	}

	public boolean effects(PredicateSymbol ps) {
		return (startEffect.effects(ps) || endEffect.effects(ps));
	}

	public Action ground(Map varMap) {
		DurativeAction a = new DurativeAction();
		a.name = this.name;

		for (Object param : params) {
			Variable v = (Variable) param;
			PDDLObject o = (PDDLObject) varMap.get(v);
			a.params.add(o);
		}

		varMap.put(duration, a.duration);

		a.duration = (DurationFunction) duration.ground(varMap);
		a.startCondition = startCondition.groundCondition(varMap);
		a.endCondition = endCondition.groundCondition(varMap);
		a.invariant = invariant.groundCondition(varMap);
		a.startEffect = startEffect.groundEffect(varMap);
		a.endEffect = endEffect.groundEffect(varMap);

		a.durationConstraint = durationConstraint.ground(varMap);

		a.startAction = new StartInstantAction();
		startAction.ground(varMap, a.startAction);

		a.endAction = new EndInstantAction();
		endAction.ground(varMap, a.endAction);

		a.dummyJoin = dummyJoin.ground(varMap);
		a.dummyGoal = dummyGoal.ground(varMap);

		a.startAction.parent = a;
		a.endAction.parent = a;

		return a;
	}


	public void makeInstants() {
		PredicateSymbol ps = new PredicateSymbol("i" + name);
		Predicate j = new Predicate(ps);
		j.addParameters(params);
		dummyJoin = j;

		PredicateSymbol ps2 = new PredicateSymbol("g" + name);
		Predicate g = new Predicate(ps2);
		g.addParameters(params);
		dummyGoal = g;

		startAction = new UngroundInstantAction();
		startAction.name = new OperatorName(name.toString() + "_START");
		startAction.params = params;
		AND s = new AND();
		s.add(startCondition);
		s.add(invariant.minus(startEffect));
		startAction.condition = s;
		AND se = new AND();
		startAction.effect = se;
		se.add(startEffect);
		se.add(j);
		se.add(new NOT(g));

		endAction = new UngroundInstantAction();
		endAction.name = new OperatorName(name.toString() + "_END");
		endAction.params = params;
		AND e = new AND();
		e.add(endCondition);
		e.add(invariant);
		e.add(j);
		endAction.condition = e;
		AND ee = new AND();
		endAction.effect = ee;
		ee.add(endEffect);
		ee.add(g);
		ee.add(new NOT(j));

	}


	public Set getStaticConditionPredicates() {
		Set rSet = startCondition.getStaticPredicates();
		rSet.addAll(endCondition.getStaticPredicates());
		rSet.addAll(invariant.getStaticPredicates());
		return rSet;
	}

	//WARNING - This is right either (at start condition) (at end effect) etc....
	public void PDDLPrint(java.io.PrintStream p, int indent) {
		p.println();
		PDDLPrinter.printIndent(p, indent);
		p.print("(:durative-action ");
		p.print(name);
		p.println();
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":parameters(\n");
		PDDLPrinter.printToString(params, p, true, false, indent + 2);
		p.println(")");
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":duration(\n");
		PDDLPrinter.printToString(durationConstraint, p, true, false, indent + 2);
		p.println(")");
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":condition");
		//condition.PDDLPrint(p, indent+2);
		p.println();
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":effect");
		//effect.PDDLPrint(p, indent+2);
		p.print(")");
	}

	public int hashCode() {
		int hash = 3;
		hash = 31 * hash ^ name.hashCode();
		hash = 31 * hash ^ params.hashCode();
		return hash;
	}

	public boolean equals(Object obj) {
		if (obj instanceof UngroundDurativeAction) {
			UngroundDurativeAction a = (UngroundDurativeAction) obj;
			return (name.equals(a.name) && params.equals(a.params));
		} else return false;
	}
}
