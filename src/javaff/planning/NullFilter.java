package javaff.planning;

import javaff.data.Action;

import java.util.HashSet;
import java.util.Set;

public class NullFilter implements Filter {
	private static NullFilter nf = null;

	private NullFilter() {
	}

	public static NullFilter getInstance() {
		if (nf == null) nf = new NullFilter(); // Singleton design pattern - return one central instance
		return nf;
	}

	public Set getActions(State S) {
		Set actionsFromS = S.getActions(); // get the logically appicable actions in S
		Set ns = new HashSet();
		// Get an iterator over these actions
		for (Object actionsFrom : actionsFromS) {
			Action a = (Action) actionsFrom;
			if (a.isApplicable(S)) ns.add(a); // Check they are applicable (will check numeric/temporal constraints)
		}
		return ns;
	}

} 