package javaff.data;

import javaff.data.metric.Function;

import java.io.PrintStream;

public class Metric implements PDDLPrintable {
	public static final int MAXIMIZE = 0;
	public static final int MINIMIZE = 1;

	public final int type;
	public final Function func;

	public Metric(int t, Function f) {
		type = t;
		func = f;
	}

	public void PDDLPrint(PrintStream p, int indent) {
		p.print("(:metric ");
		p.print(toString());
		p.print(")");
	}

	public String toString() {
		String str = "";
		if (type == MAXIMIZE) str += "maximize ";
		else if (type == MINIMIZE) str += "minimize ";
		str += func.toString();
		return str;
	}

	public String toStringTyped() {
		String str = "";
		if (type == MAXIMIZE) str += "maximize ";
		else if (type == MINIMIZE) str += "minimize ";
		str += func.toStringTyped();
		return str;
	}
}
