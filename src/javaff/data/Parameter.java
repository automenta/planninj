package javaff.data;

import javaff.data.strips.SimpleType;

import java.io.PrintStream;

public abstract class Parameter implements PDDLPrintable {
	protected String name;
	protected Type type;

	public Parameter(String n) {
		setName(n);
		setRootType();
	}

	public Parameter(String n, Type t) {
		setName(n);
		setType(t);
	}

	public void setRootType() {
		type = SimpleType.rootType;
	}

	public void setDetails(String n, Type t) {
		setName(n);
		setType(t);
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = n;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type t) {
		type = t;
	}

	public boolean isOfType(Type t) // is this of Type t
	{
		return type.isOfType(t);
	}

	public String toString() {
		return name;
	}

	public String toStringTyped() {
		return name + " - " + type.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof Parameter) {
			Parameter po = (Parameter) obj;
			return (name.equals(po.name) && type.equals(po.type) && this.getClass() == po.getClass());
		} else return false;
	}

	public void PDDLPrint(PrintStream p, int indent) {
		PDDLPrinter.printToString(this, p, false, false, indent);
	}
}
