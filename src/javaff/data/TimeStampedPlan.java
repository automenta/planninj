package javaff.data;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

public class TimeStampedPlan implements Plan {
	public final SortedSet actions = new TreeSet();

	public void addAction(Action a, BigDecimal t) {
		addAction(a, t, null);
	}

	public void addAction(Action a, BigDecimal t, BigDecimal d) {
		actions.add(new TimeStampedAction(a, t, d));
	}


	public void print(PrintStream p) {
		for (Object action : actions) {
			TimeStampedAction a = (TimeStampedAction) action;
			p.println(a);
		}
	}

	public void print(PrintWriter p) {
		for (Object action : actions) {
			TimeStampedAction a = (TimeStampedAction) action;
			p.println(a);
		}
	}

	public Set getActions() {
		Set s = new HashSet();
		for (Object action : actions) {
			TimeStampedAction a = (TimeStampedAction) action;
			s.add(a.action);
		}
		return s;
	}
}
