package javaff.data;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Set;

public interface Plan extends Cloneable {
	void print(PrintStream p);

	void print(PrintWriter p);

	Set getActions();
}
