package javaff.scheduling;

import javaff.data.strips.InstantAction;

import java.util.Set;

public interface SimpleTemporalNetwork {
	void addConstraints(Set constraints);

	void addConstraint(TemporalConstraint c);

	boolean consistentSource(InstantAction s);

	boolean consistent();
}
