package javaff.data.strips;

import javaff.data.*;
import javaff.planning.STRIPSState;
import javaff.planning.State;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NOT implements CompoundLiteral, GroundEffect, UngroundEffect {
	final Literal literal;

	public NOT(Literal l) {
		literal = l;
	}

	public void apply(State s) {
		STRIPSState ss = (STRIPSState) s;
		ss.removeProposition((Proposition) literal);
	}

	public void applyAdds(State s) {
	}

	public void applyDels(State s) {
		apply(s);
	}

	public boolean effects(PredicateSymbol ps) {
		UngroundEffect ue = (UngroundEffect) literal;
		return ue.effects(ps);
	}

	public UngroundCondition effectsAdd(UngroundCondition cond) {
		return cond;
	}

	public GroundEffect groundEffect(Map varMap) {
		Predicate p = (Predicate) literal;
		return new NOT((Proposition) p.groundEffect(varMap));
	}

	public Set getAddPropositions() {
		return new HashSet();
	}

	public Set getDeletePropositions() {
		Set rSet = new HashSet();
		rSet.add(literal);
		return rSet;
	}

	public Set getOperators() {
		return new HashSet();
	}


	public boolean equals(Object obj) {
		if (obj instanceof NOT) {
			NOT n = (NOT) obj;
			return (literal.equals(n.literal));
		} else return false;
	}

	public GroundEffect staticifyEffect(Map fValues) {
		return this;
	}

	public int hashCode() {
		return literal.hashCode() ^ 2;
	}

	public String toString() {
		return "not (" + literal.toString() + ")";
	}

	public String toStringTyped() {
		return "not (" + literal.toStringTyped() + ")";
	}


	public void PDDLPrint(java.io.PrintStream p, int indent) {
		p.print("(not ");
		PDDLPrinter.printToString(literal, p, false, true, indent);
		p.print(")");
	}

}
