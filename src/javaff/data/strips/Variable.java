package javaff.data.strips;

import javaff.data.Type;

public class Variable extends javaff.data.Parameter {
	public Variable(String n) {
		super(n);
	}

	public Variable(String n, Type t) {
		super(n, t);
	}


	public int hashCode() {
		int hash = 9;
		hash = 31 * hash ^ name.hashCode();
		hash = 31 * hash ^ type.hashCode();
		return hash;
	}
}
