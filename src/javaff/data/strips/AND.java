package javaff.data.strips;

import javaff.data.*;
import javaff.planning.State;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AND implements CompoundLiteral, GroundCondition, GroundEffect, UngroundCondition, UngroundEffect {
	protected Set literals = new HashSet(); // set of Literals

	public void add(Object o) {
		if (o instanceof AND) {
			AND a = (AND) o;
			for (Object literal : a.literals) {
				add(literal);
			}
		} else literals.add(o);
	}

	public boolean isStatic() {
		for (Object literal : literals) {
			Condition c = (Condition) literal;
			if (!c.isStatic()) return false;
		}
		return true;
	}

	public GroundCondition staticifyCondition(Map fValues) {
		Set newlit = new HashSet(literals.size());
		for (Object literal : literals) {
			GroundCondition c = (GroundCondition) literal;
			if (!(c instanceof TrueCondition)) newlit.add(c.staticifyCondition(fValues));
		}
		literals = newlit;
		if (literals.isEmpty()) return TrueCondition.getInstance();
		else return this;
	}

	public GroundEffect staticifyEffect(Map fValues) {
		Set newlit = new HashSet(literals.size());
		for (Object literal : literals) {
			GroundEffect e = (GroundEffect) literal;
			if (!(e instanceof NullEffect)) newlit.add(e.staticifyEffect(fValues));
		}
		literals = newlit;
		if (literals.isEmpty()) return NullEffect.getInstance();
		else return this;
	}

	public Set getStaticPredicates() {
		Set rSet = new HashSet();
		for (Object literal : literals) {
			UngroundCondition c = (UngroundCondition) literal;
			rSet.addAll(c.getStaticPredicates());
		}
		return rSet;
	}

	public boolean effects(PredicateSymbol ps) {
		boolean rEff = false;
		Iterator lit = literals.iterator();
		while (lit.hasNext() && !(rEff)) {
			UngroundEffect ue = (UngroundEffect) lit.next();
			rEff = ue.effects(ps);
		}
		return rEff;
	}

	public UngroundCondition minus(UngroundEffect effect) {
		AND a = new AND();
		for (Object literal : literals) {
			UngroundCondition p = (UngroundCondition) literal;
			a.add(p.minus(effect));
		}
		return a;
	}

	public UngroundCondition effectsAdd(UngroundCondition cond) {
		Iterator lit = literals.iterator();
		UngroundCondition c = null;
		while (lit.hasNext()) {
			UngroundEffect p = (UngroundEffect) lit.next();
			UngroundCondition d = p.effectsAdd(cond);
			if (!d.equals(cond)) c = d;
		}
		if (c == null) return cond;
		else return c;
	}


	public GroundEffect groundEffect(Map varMap) {
		AND a = new AND();
		for (Object literal : literals) {
			UngroundEffect p = (UngroundEffect) literal;
			a.add(p.groundEffect(varMap));
		}
		return a;
	}

	public GroundCondition groundCondition(Map varMap) {
		AND a = new AND();
		for (Object literal : literals) {
			UngroundCondition p = (UngroundCondition) literal;
			a.add(p.groundCondition(varMap));
		}
		return a;
	}

	public boolean isTrue(State s) {
		for (Object literal : literals) {
			GroundCondition c = (GroundCondition) literal;
			if (!c.isTrue(s)) return false;
		}
		return true;
	}

	public void apply(State s) {
		applyDels(s);
		applyAdds(s);
	}

	public void applyAdds(State s) {
		for (Object literal : literals) {
			GroundEffect e = (GroundEffect) literal;
			e.applyAdds(s);
		}
	}

	public void applyDels(State s) {
		for (Object literal : literals) {
			GroundEffect e = (GroundEffect) literal;
			e.applyDels(s);
		}
	}

	public Set getConditionalPropositions() {
		Set rSet = new HashSet();
		for (Object literal : literals) {
			GroundCondition e = (GroundCondition) literal;
			rSet.addAll(e.getConditionalPropositions());
		}
		return rSet;
	}

	public Set getAddPropositions() {
		Set rSet = new HashSet();
		for (Object literal : literals) {
			GroundEffect e = (GroundEffect) literal;
			rSet.addAll(e.getAddPropositions());
		}
		return rSet;
	}

	public Set getDeletePropositions() {
		Set rSet = new HashSet();
		for (Object literal : literals) {
			GroundEffect e = (GroundEffect) literal;
			rSet.addAll(e.getDeletePropositions());
		}
		return rSet;
	}

	public Set getOperators() {
		Set rSet = new HashSet();
		for (Object literal : literals) {
			GroundEffect e = (GroundEffect) literal;
			rSet.addAll(e.getOperators());
		}
		return rSet;
	}

	public Set getComparators() {
		Set rSet = new HashSet();
		for (Object literal : literals) {
			GroundCondition e = (GroundCondition) literal;
			rSet.addAll(e.getComparators());
		}
		return rSet;
	}


	public boolean equals(Object obj) {
		if (obj instanceof AND) {
			AND a = (AND) obj;
			return (literals.equals(a.literals));
		} else return false;
	}

	public int hashCode() {
		return literals.hashCode();
	}


	public void PDDLPrint(PrintStream p, int indent) {
		PDDLPrinter.printToString(literals, "and", p, false, true, indent);
	}

	public String toString() {
		String str = "(and";
		for (Object literal : literals) {
			str += " " + literal;
		}
		str += ")";
		return str;
	}

	public String toStringTyped() {
		String str = "(and";
		for (Object literal : literals) {
			Literal l = (Literal) literal;
			str += " " + l.toStringTyped();
		}
		str += ")";
		return str;

	}
}
