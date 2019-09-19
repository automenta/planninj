package javaff.data.strips;

import javaff.data.PDDLPrintable;
import javaff.data.PDDLPrinter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class PredicateSymbol implements PDDLPrintable {
	protected String name;
	protected boolean staticValue;

	protected final List params = new ArrayList(); //The parameters (types) that this predicate symbol takes

	protected PredicateSymbol() {

	}

	public PredicateSymbol(String pName) {
		name = pName;
	}

	public String toString() {
		return name;
	}

	public String toStringTyped() {
		String str = name;
		for (Object param : params) {
			Variable v = (Variable) param;
			str += " " + v.toStringTyped();
		}
		return str;
	}

	public boolean isStatic() {
		return staticValue;
	}

	public void setStatic(boolean stat) {
		staticValue = stat;
	}

	public void addVar(Variable v) {
		params.add(v);
	}

	public boolean equals(Object obj) {
		if (obj instanceof PredicateSymbol) {
			PredicateSymbol ps = (PredicateSymbol) obj;
			return (name.equals(ps.name) && params.equals(ps.params));
		} else return false;
	}

	public int hashCode() {
		int hash = 8;
		hash = 31 * hash ^ name.hashCode();
		hash = 31 * hash ^ params.hashCode();
		return hash;
	}

	public void PDDLPrint(PrintStream p, int indent) {
		PDDLPrinter.printToString(this, p, true, true, indent);
	}
}
