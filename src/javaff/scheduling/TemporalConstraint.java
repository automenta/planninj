package javaff.scheduling;

import javaff.data.strips.InstantAction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TemporalConstraint extends Constraint {
	final InstantAction x;
	final InstantAction y;
	final BigDecimal b;

	public TemporalConstraint(InstantAction X, InstantAction Y, BigDecimal B) {
		x = X;
		y = Y;
		b = B;
	}

	public static TemporalConstraint getConstraint(InstantAction first, InstantAction second) {
		return new TemporalConstraint(first, second, javaff.JavaFF.EPSILON.negate());
	}

	public static TemporalConstraint getConstraintEqual(InstantAction first, InstantAction second) {
		return new TemporalConstraint(first, second, new BigDecimal(0));
	}

	public static TemporalConstraint getConstraintMax(InstantAction first, InstantAction second, BigDecimal max) {
		return new TemporalConstraint(second, first, max);
	}

	public static TemporalConstraint getConstraintMin(InstantAction first, InstantAction second, BigDecimal min) {
		return new TemporalConstraint(first, second, min.negate());
	}

	public static List getExactly(InstantAction first, InstantAction second, BigDecimal value) {
		List rList = new ArrayList(2);
		rList.add(getConstraintMax(first, second, value));
		rList.add(getConstraintMin(first, second, value));
		return rList;
	}

	public static List getBounds(InstantAction first, InstantAction second, BigDecimal max, BigDecimal min) {
		List rList = new ArrayList(2);
		rList.add(getConstraintMax(first, second, max));
		rList.add(getConstraintMin(first, second, min));
		return rList;
	}

	public String toString() {
		return (x.toString() + " - " + y.toString() + " <= " + b.toString());
	}

	public boolean equals(Object obj) {
		if (obj instanceof TemporalConstraint) {
			TemporalConstraint c = (TemporalConstraint) obj;
			return (c.x.equals(x) && c.y.equals(y) && c.b.equals(b));
		}
		return false;
	}

	public int hashCode() {
		int hash = 2;
		hash = 31 * hash ^ x.hashCode();
		hash = 31 * hash ^ y.hashCode();
		hash = 31 * hash ^ b.hashCode();
		return hash;
	}
}
