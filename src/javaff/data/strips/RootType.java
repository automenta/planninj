// A Singleton to represent the default "object" type for the root of the type hierarchy

package javaff.data.strips;

import javaff.data.Type;

public class RootType extends SimpleType {
	private static RootType r;

	private RootType() {
		name = "object";
		superType = null;
	}

	public static RootType getInstance() {
		if (r == null) r = new RootType();
		return r;
	}

	public boolean isOfType(Type t) {
		return t.equals(this);
	}

	public void setSuperType(Type pSuperType) {
		return;
	}
}
