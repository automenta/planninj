package javaff.data.metric;

import javaff.data.GroundCondition;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundCondition;
import javaff.data.UngroundEffect;
import javaff.planning.MetricState;
import javaff.planning.State;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BinaryComparator implements javaff.data.GroundCondition, javaff.data.UngroundCondition {
	public Function first, second;
	public final int type;

	public BinaryComparator(String s, Function f1, Function f2) {
		type = MetricSymbolStore.getType(s);
		first = f1;
		second = f2;
	}

	public BinaryComparator(int t, Function f1, Function f2) {
		type = t;
		first = f1;
		second = f2;
	}

	public boolean isStatic() {
		return (first.isStatic() && second.isStatic());
	}

	public boolean effectedBy(ResourceOperator ro) {
		return (first.effectedBy(ro) || second.effectedBy(ro));
	}

	public GroundCondition staticifyCondition(Map fValues) {
		first = first.staticify(fValues);
		second = second.staticify(fValues);
		return this;
	}

	public UngroundCondition minus(UngroundEffect effect) {
		return effect.effectsAdd(this);
	}

	public Set getStaticPredicates() {
		return new HashSet();
	}

	public GroundCondition groundCondition(Map varMap) {
		return new BinaryComparator(type, first.ground(varMap), second.ground(varMap));
	}

	public boolean isTrue(State s) {
		MetricState ms = (MetricState) s;
		BigDecimal df = first.getValue(ms);
		BigDecimal ds = second.getValue(ms);

		boolean result = false;

		if (type == MetricSymbolStore.GREATER_THAN) result = (df.compareTo(ds) > 0);
		else if (type == MetricSymbolStore.GREATER_THAN_EQUAL) result = (df.compareTo(ds) >= 0);
		else if (type == MetricSymbolStore.LESS_THAN) result = (df.compareTo(ds) < 0);
		else if (type == MetricSymbolStore.LESS_THAN_EQUAL) result = (df.compareTo(ds) <= 0);
		else if (type == MetricSymbolStore.EQUAL) result = (df.compareTo(ds) == 0);

		return result;
	}

	public void PDDLPrint(PrintStream ps, int i) {
		PDDLPrinter.printToString(this, ps, false, false, i);
	}

	public Set getConditionalPropositions() {
		return new HashSet();
	}

	public Set getComparators() {
		Set s = new HashSet();
		s.add(this);
		return s;
	}


	public String toString() {
		return MetricSymbolStore.getSymbol(type) + " " + first.toString() + " " + second.toString();
	}

	public String toStringTyped() {
		return MetricSymbolStore.getSymbol(type) + " " + first.toStringTyped() + " " + second.toStringTyped();
	}


	public boolean equals(Object obj) {
		if (obj instanceof BinaryComparator) {
			BinaryComparator bc = (BinaryComparator) obj;
			if (bc.type == this.type && first.equals(bc.first) && second.equals(bc.second)) return true;
			else return ((bc.type == MetricSymbolStore.LESS_THAN && this.type == MetricSymbolStore.GREATER_THAN) ||
				(this.type == MetricSymbolStore.LESS_THAN && bc.type == MetricSymbolStore.GREATER_THAN) ||
				(this.type == MetricSymbolStore.LESS_THAN_EQUAL && bc.type == MetricSymbolStore.GREATER_THAN_EQUAL) ||
				(this.type == MetricSymbolStore.LESS_THAN_EQUAL && bc.type == MetricSymbolStore.GREATER_THAN_EQUAL)) &&
				(first.equals(bc.second) && second.equals(bc.first));
		} else return false;
	}

}
