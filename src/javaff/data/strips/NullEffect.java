package javaff.data.strips;

import javaff.data.GroundEffect;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundCondition;
import javaff.data.UngroundEffect;
import javaff.planning.State;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NullEffect implements UngroundEffect, GroundEffect {
	private static NullEffect n;

	private NullEffect() {
	}

	public static NullEffect getInstance() {
		if (n == null) n = new NullEffect();
		return n;
	}

	public boolean effects(PredicateSymbol ps) {
		return false;
	}

	public UngroundCondition effectsAdd(UngroundCondition cond) {
		return cond;
	}

	public GroundEffect groundEffect(Map varMap) {
		return this;
	}

	public void apply(State s) {

	}

	public void applyAdds(State s) {

	}

	public void applyDels(State s) {

	}

	public GroundEffect staticifyEffect(Map fValues) {
		return this;
	}


	public Set getAddPropositions() {
		return new HashSet();
	}

	public Set getDeletePropositions() {
		return new HashSet();
	}

	public Set getOperators() {
		return new HashSet();
	}

	public String toString() {
		return "()";
	}

	public String toStringTyped() {
		return toString();
	}

	public void PDDLPrint(PrintStream p, int indent) {
		PDDLPrinter.printToString(this, p, false, false, indent);
	}
}
