package javaff.data;

import javaff.data.strips.PredicateSymbol;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public abstract class Literal implements Condition, Effect {
	protected PredicateSymbol name;
	protected final List parameters = new ArrayList(); // list of Parameters

	public PredicateSymbol getPredicateSymbol() {
		return name;
	}

	public void setPredicateSymbol(PredicateSymbol n) {
		name = n;
	}

	public List getParameters() {
		return parameters;
	}

	public void addParameter(Parameter p) {
		parameters.add(p);
	}

	public void addParameters(List l) {
		parameters.addAll(l);
	}

	public String toString() {
		String stringrep = name.toString();
		for (Object parameter : parameters) {
			stringrep = stringrep + " " + parameter;
		}
		return stringrep;
	}

	public String toStringTyped() {
		String stringrep = name.toString();
		for (Object parameter : parameters) {
			Parameter o = (Parameter) parameter;
			stringrep += " " + o + " - " + o.type.toString();
		}
		return stringrep;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Literal) {
			Literal p = (Literal) obj;
			return (name.equals(p.name) && parameters.equals(p.parameters) && this.getClass() == p.getClass());
		} else return false;
	}


	public boolean isStatic() {
		return name.isStatic();
	}

	public void PDDLPrint(PrintStream p, int indent) {
		PDDLPrinter.printToString(this, p, false, true, indent);
	}
}
