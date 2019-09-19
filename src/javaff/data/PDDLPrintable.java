package javaff.data;

import java.io.PrintStream;

public interface PDDLPrintable {
	void PDDLPrint(PrintStream p, int indent);

	String toStringTyped();
}
