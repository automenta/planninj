package javaff.data;

import javaff.planning.State;

import java.util.Map;
import java.util.Set;

public interface GroundEffect extends Effect {
	void apply(State s); // carry out the effects of this on State s

	void applyAdds(State s);

	void applyDels(State s);

	Set getAddPropositions();

	Set getDeletePropositions();

	Set getOperators();

	GroundEffect staticifyEffect(Map fValues);
}
