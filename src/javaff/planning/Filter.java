package javaff.planning;

import java.util.Set;

public interface Filter {
	Set getActions(State S); // simple method: takes a state S, returns a Set of states in its neighbourhood
} 