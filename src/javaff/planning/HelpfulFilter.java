package javaff.planning;

import javaff.data.Action;

import java.util.HashSet;
import java.util.Set;

public class HelpfulFilter implements Filter {
	private static HelpfulFilter hf = null;

	private HelpfulFilter() {
	}

	public static HelpfulFilter getInstance() {
		if (hf == null) hf = new HelpfulFilter(); // Singleton, as in NullFilter
		return hf;
	}

	public Set getActions(State S) {
		STRIPSState SS = (STRIPSState) S;
		SS.calculateRP(); // get the relaxed plan to the goal, to make sure helpful actions exist for S
		Set ns = new HashSet();
		// iterate over helpful actions
		for (Object o : SS.helpfulActions) {
			Action a = (Action) o;
			if (a.isApplicable(S)) ns.add(a); // and add them to the set to return if they're applicable
		}
		return ns;
	}
} 