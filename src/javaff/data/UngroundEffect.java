package javaff.data;

import javaff.data.strips.PredicateSymbol;

import java.util.Map;

public interface UngroundEffect extends Effect {
	boolean effects(PredicateSymbol ps);

	UngroundCondition effectsAdd(UngroundCondition cond);

	GroundEffect groundEffect(Map varMap);
}
