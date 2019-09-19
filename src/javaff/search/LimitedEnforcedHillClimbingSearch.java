package javaff.search;

import javaff.planning.State;

import java.math.BigDecimal;
import java.util.Comparator;

public class LimitedEnforcedHillClimbingSearch extends EnforcedHillClimbingSearch {
	private final BigDecimal planSizeLimit;
	private BigDecimal currentHValue;

	public LimitedEnforcedHillClimbingSearch(State s, BigDecimal l) {
		this(s, new HValueComparator(), l);
	}

	public LimitedEnforcedHillClimbingSearch(State s, Comparator c, BigDecimal l) {
		super(s);
		setComparator(c);
		planSizeLimit = l;
	}

	public boolean needToVisit(State s) {
		if (s.getGValue().compareTo(planSizeLimit) > 0) return false; // if length bound has been exceeded...

		return super.needToVisit(s);
	}
}
