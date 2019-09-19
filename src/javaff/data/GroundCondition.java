package javaff.data;

import javaff.planning.State;

import java.util.Map;
import java.util.Set;

public interface GroundCondition extends Condition {
	boolean isTrue(State s); // returns whether this conditions is true is State S

	Set getConditionalPropositions();

	Set getComparators();

	GroundCondition staticifyCondition(Map fValues);
}
