package javaff.search;

import javaff.planning.State;

import java.util.Comparator;

public abstract class Search {
	protected final State start;
	protected int nodeCount = 0;
	protected Comparator comp;

	public Search(State s) {
		start = s;
	}

	public Comparator getComparator() {
		return comp;
	}

	public void setComparator(Comparator c) {
		comp = c;
	}

	public abstract State search();


}

