package javaff.data.metric;

import javaff.data.GroundEffect;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundCondition;
import javaff.data.strips.PredicateSymbol;
import javaff.planning.MetricState;
import javaff.planning.State;
import javaff.scheduling.MatrixSTN;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResourceOperator implements javaff.data.GroundEffect, javaff.data.UngroundEffect {
	public NamedFunction resource; // will be a named function
	public Function change;
	public int type;

	protected ResourceOperator() {

	}

	public ResourceOperator(String s, NamedFunction r, Function c) {
		type = MetricSymbolStore.getType(s);
		resource = r;
		change = c;
	}

	public ResourceOperator(int t, NamedFunction r, Function c) {
		type = t;
		resource = r;
		change = c;
	}

	public void apply(State s) {
		MetricState ms = (MetricState) s;
		BigDecimal dr = resource.getValue(ms);
		BigDecimal dc = change.getValue(ms);
		BigDecimal res = null;

		if (type == MetricSymbolStore.ASSIGN) res = dc;
		else if (type == MetricSymbolStore.INCREASE) res = dr.add(dc);
		else if (type == MetricSymbolStore.DECREASE) res = dr.subtract(dc);
		else if (type == MetricSymbolStore.SCALE_UP) res = dr.multiply(dc);
		else if (type == MetricSymbolStore.SCALE_DOWN)
			res = dr.divide(dc, MetricSymbolStore.SCALE, MetricSymbolStore.ROUND);
		ms.setValue(resource, res);
	}

	public BigDecimal applyMax(BigDecimal d, MatrixSTN stn) {
		BigDecimal dr = d;
		BigDecimal dc = change.getMaxValue(stn);
		BigDecimal res = null;

		if (type == MetricSymbolStore.ASSIGN) res = dc;
		else if (type == MetricSymbolStore.INCREASE) res = dr.add(dc);
		else if (type == MetricSymbolStore.DECREASE) res = dr.subtract(dc);
		else if (type == MetricSymbolStore.SCALE_UP) res = dr.multiply(dc);
		else if (type == MetricSymbolStore.SCALE_DOWN)
			res = dr.divide(dc, MetricSymbolStore.SCALE, MetricSymbolStore.ROUND);
		return res;
	}

	public BigDecimal applyMin(BigDecimal d, MatrixSTN stn) {
		BigDecimal dr = d;
		BigDecimal dc = change.getMinValue(stn);
		BigDecimal res = null;

		if (type == MetricSymbolStore.ASSIGN) res = dc;
		else if (type == MetricSymbolStore.INCREASE) res = dr.add(dc);
		else if (type == MetricSymbolStore.DECREASE) res = dr.subtract(dc);
		else if (type == MetricSymbolStore.SCALE_UP) res = dr.multiply(dc);
		else if (type == MetricSymbolStore.SCALE_DOWN)
			res = dr.divide(dc, MetricSymbolStore.SCALE, MetricSymbolStore.ROUND);
		return res;
	}

	public void applyAdds(State s) {
		apply(s);
	}

	public void applyDels(State s) {

	}

	public GroundEffect staticifyEffect(Map fValues) {
		change = change.staticify(fValues);
		return this;
	}

	public GroundEffect groundEffect(Map varMap) {
		return new ResourceOperator(type, (NamedFunction) resource.ground(varMap), change.ground(varMap));
	}

	public boolean effects(PredicateSymbol p) {
		if (p instanceof FunctionSymbol) {
			FunctionSymbol f = (FunctionSymbol) p;
			return (resource.getPredicateSymbol().equals(f));
		} else return false;
	}

	public UngroundCondition effectsAdd(UngroundCondition cond) {
		if (cond instanceof BinaryComparator) {
			BinaryComparator bc = (BinaryComparator) cond;
			if (bc.effectedBy(this)) {
				Function l = bc.first;
				Function r = bc.second;
				if (bc.first.effectedBy(this)) {
					l = bc.first.replace(this);
				}
				if (bc.second.effectedBy(this)) {
					r = bc.second.replace(this);
				}
				return new BinaryComparator(bc.type, l, r);
			} else return cond;

		} else return cond;
	}

	public Set getAddPropositions() {
		return new HashSet();
	}

	public Set getDeletePropositions() {
		return new HashSet();
	}


	public Set getOperators() {
		Set s = new HashSet();
		s.add(this);
		return s;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ResourceOperator) {
			ResourceOperator ro = (ResourceOperator) obj;
			return ro.type == this.type && resource.equals(ro.resource) && change.equals(ro.change);
		} else return false;
	}

	public String toString() {
		return MetricSymbolStore.getSymbol(type) + " " + resource.toString() + " " + change.toString();
	}

	public String toStringTyped() {
		return MetricSymbolStore.getSymbol(type) + " " + resource.toStringTyped() + " " + change.toStringTyped();
	}

	public void PDDLPrint(PrintStream p, int indent) {
		PDDLPrinter.printToString(this, p, false, false, indent);
	}
}

