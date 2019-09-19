package javaff.data.strips;

import javaff.data.Action;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundCondition;
import javaff.data.UngroundEffect;

import java.util.Map;
import java.util.Set;

public class UngroundInstantAction extends Operator {
	public UngroundCondition condition;
	public UngroundEffect effect;

	public boolean effects(PredicateSymbol ps) {
		return effect.effects(ps);
	}

	public Action ground(Map varMap) {
		return ground(varMap, new STRIPSInstantAction());
	}

	public Action ground(Map varMap, InstantAction a) {
		a.name = this.name;

		for (Object param : params) {
			Variable v = (Variable) param;
			PDDLObject o = (PDDLObject) varMap.get(v);
			a.params.add(o);
		}
		a.condition = condition.groundCondition(varMap);
		a.effect = effect.groundEffect(varMap);
		return a;
	}

	public Set getStaticConditionPredicates() {
		return condition.getStaticPredicates();
	}

	public void PDDLPrint(java.io.PrintStream p, int indent) {
		p.println();
		PDDLPrinter.printIndent(p, indent);
		p.print("(:action ");
		p.print(name);
		p.println();
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":parameters(\n");
		PDDLPrinter.printToString(params, p, true, false, indent + 2);
		p.println(")");
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":precondition");
		condition.PDDLPrint(p, indent + 2);
		p.println();
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":effect");
		effect.PDDLPrint(p, indent + 2);
		p.print(")");
	}

}
