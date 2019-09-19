package javaff.data.temporal;

import javaff.data.PDDLPrinter;
import javaff.data.metric.Function;
import javaff.data.metric.MetricSymbolStore;
import javaff.planning.MetricState;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Map;

public class SimpleDurationConstraint extends DurationConstraint {
	protected final int type;
	protected final DurationFunction variable;
	protected final Function value;

	public SimpleDurationConstraint(DurationFunction v, Function f, int t) {
		type = t;
		variable = v;
		value = f;
	}

	public DurationConstraint ground(Map varMap) {
		return new SimpleDurationConstraint((DurationFunction) variable.ground(varMap), value.ground(varMap), type);
	}

	public BigDecimal getDuration(MetricState ms) {
		return value.getValue(ms);
	}

	//could put stuff about < and > using epsilon
	public BigDecimal getMaxDuration(MetricState ms) {
		if (type == MetricSymbolStore.LESS_THAN_EQUAL) return value.getValue(ms);
		else if (type == MetricSymbolStore.GREATER_THAN_EQUAL) return javaff.JavaFF.MAX_DURATION;
		else if (type == MetricSymbolStore.EQUAL) return value.getValue(ms);

		else return null;
	}

	public BigDecimal getMinDuration(MetricState ms) {
		if (type == MetricSymbolStore.LESS_THAN_EQUAL) return new BigDecimal(0);
		else if (type == MetricSymbolStore.GREATER_THAN_EQUAL) return value.getValue(ms);
		else if (type == MetricSymbolStore.EQUAL) return value.getValue(ms);
		else return null;
	}

	public boolean staticDuration() {
		//return value.isStatic();
		return (type == MetricSymbolStore.EQUAL);
	}


	public void addConstraint(SimpleDurationConstraint sdc) {

	}

	public void PDDLPrint(PrintStream p, int indent) {
		PDDLPrinter.printToString(this, p, true, false, indent);
	}

	public String toString() {
		String str = "(" + MetricSymbolStore.getSymbol(type) + " " + variable.toString() + " " + value.toString() + ")";
		return str;
	}

	public String toStringTyped() {
		String str = "(" + MetricSymbolStore.getSymbol(type) + " " + variable.toStringTyped() + " " + value.toStringTyped() + ")";
		return str;
	}

	public int hashCode() {
		int hash = 7;
		hash = 31 * hash ^ type;
		hash = 31 * hash ^ variable.hashCode();
		hash = 31 * hash ^ value.hashCode();
		return hash;
	}

	public boolean equals(Object obj) {
		if (obj instanceof SimpleDurationConstraint) {
			SimpleDurationConstraint c = (SimpleDurationConstraint) obj;
			return (type == c.type && variable.equals(c.variable) && value.equals(c.value));
		} else return false;
	}

}
