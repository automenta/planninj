package javaff.data.strips;


import javaff.data.GroundCondition;
import javaff.data.GroundEffect;
import javaff.planning.STRIPSState;
import javaff.planning.State;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Proposition extends javaff.data.Literal implements GroundCondition, GroundEffect {
	public Proposition(PredicateSymbol p) {
		name = p;
	}

	public boolean isTrue(State s) // returns whether this conditions is true is State S
	{
		STRIPSState ss = (STRIPSState) s;
		return ss.isTrue(this);
	}

	public void apply(State s) // carry out the effects of this on State s
	{
		STRIPSState ss = (STRIPSState) s;
		ss.addProposition(this);
	}

	public void applyAdds(State s) {
		apply(s);
	}

	public void applyDels(State s) {
	}

	public boolean isStatic() {
		return name.isStatic();
	}

	public Set getDeletePropositions() {
		return new HashSet();
	}

	public Set getAddPropositions() {
		Set rSet = new HashSet();
		rSet.add(this);
		return rSet;
	}

	public GroundCondition staticifyCondition(Map fValues) {
		if (isStatic()) return TrueCondition.getInstance();
		else return this;
	}

	public GroundEffect staticifyEffect(Map fValues) {
		return this;
	}


	public Set getConditionalPropositions() {
		Set rSet = new HashSet();
		rSet.add(this);
		return rSet;
	}

	public Set getOperators() {
		return new HashSet();
	}

	public Set getComparators() {
		return new HashSet();
	}

	public int hashCode() {
		int hash = 6;
		hash = 31 * hash ^ name.hashCode();
		hash = 31 * hash ^ parameters.hashCode();
		return hash;
	}
}
