package javaff.data;

import java.math.BigDecimal;

public class TimeStampedAction implements Comparable {
	public final Action action;
	public final BigDecimal time;
	public final BigDecimal duration;

	public TimeStampedAction(Action a, BigDecimal t, BigDecimal d) {
		action = a;
		time = t;
		duration = d;
	}

	public String toString() {
		String str = time + ": (" + action + ")";
		if (duration != null) str += " [" + duration + "]";
		return str;
	}

	public int compareTo(Object o) {
		TimeStampedAction that = (TimeStampedAction) o;
		if (this.time.compareTo(that.time) != 0) return this.time.compareTo(that.time);
		return (Integer.compare(this.action.hashCode(), that.action.hashCode()));
	}
}
