package javaff.data.strips;

import javaff.data.Type;

import java.util.HashSet;
import java.util.Set;

public class EitherType extends Type {
	protected final Set types = new HashSet();

	public void addType(SimpleType t) {
		types.add(t);
	}

	public String toString() {
		String str = "(either";
		for (Object type : types) {
			str += " " + type;
		}
		str += ")";
		return str;
	}

	public String toStringTyped() {
		return toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof EitherType) {
			EitherType et = (EitherType) obj;
			return (types.equals(et.types));
		} else return false;
	}

	public boolean isOfType(Type t) // is this of type t (i.e. is type further up the hierarchy)
	{
		for (Object type : types) {
			SimpleType st = (SimpleType) type;
			if (st.isOfType(t)) return true;
		}
		return false;
	}

}
