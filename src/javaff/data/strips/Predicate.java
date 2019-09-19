package javaff.data.strips;

import javaff.data.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Predicate extends Literal implements UngroundCondition, UngroundEffect {
	public Predicate(PredicateSymbol p) {
		name = p;
	}

	public boolean effects(PredicateSymbol ps) {
		return name.equals(ps);
	}

	public UngroundCondition minus(UngroundEffect effect) {
		return effect.effectsAdd(this);
	}

	public UngroundCondition effectsAdd(UngroundCondition cond) {
		if (this.equals(cond)) return TrueCondition.getInstance();
		else return cond;
	}

	public Set getStaticPredicates() {
		Set rSet = new HashSet();
		if (name.isStatic()) rSet.add(this);
		return rSet;
	}

	public Proposition ground(Map varMap) {
		Proposition p = new Proposition(name);
		for (Object o : parameters) {
			PDDLObject po;
			if (o instanceof PDDLObject) po = (PDDLObject) o;
			else {
				Variable v = (Variable) o;
				po = (PDDLObject) varMap.get(v);
			}

			p.addParameter(po);
		}
		return p;
	}

	public GroundCondition groundCondition(Map varMap) {
		return ground(varMap);
	}

	public GroundEffect groundEffect(Map varMap) {
		return ground(varMap);
	}

	public int hashCode() {
		int hash = 5;
		hash = 31 * hash ^ name.hashCode();
		hash = 31 * hash ^ parameters.hashCode();
		return hash;
	}

}
