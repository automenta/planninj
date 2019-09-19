package javaff.data.strips;

import javaff.data.Type;

public class SimpleType extends Type {
	protected String name;
	protected Type superType;

	protected SimpleType() {
	}

	public SimpleType(String pName) {
		name = pName;
		superType = rootType;
	}

	public SimpleType(String pName, Type pSuperType) {
		name = pName;
		superType = pSuperType;
	}

	public void setSuperType(Type pSuperType) {
		superType = pSuperType;
	}

	public String toString() {
		return name;
	}

	public String toStringTyped() {
		return name + " - " + superType;
	}

	public boolean equals(Object obj) {
		if (obj instanceof SimpleType) {
			SimpleType ty = (SimpleType) obj;
			return (name.equals(ty.name));
		} else return false;
	}

	public int hashCode() {
		return 31 * 8 + name.hashCode();
	}

	public boolean isOfType(Type t) // is this of type t (i.e. is type further up the hierarchy)
	{
		if (this.equals(t)) return true;
		else return superType.isOfType(t);
	}
}
