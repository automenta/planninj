package javaff.data.strips;

import javaff.data.Action;
import javaff.data.UngroundProblem;

import java.util.*;

public abstract class Operator implements javaff.data.PDDLPrintable {
	public OperatorName name;
	public List params = new ArrayList(); // list of Variables

	public String toString() {
		String stringrep = name.toString();
		for (Object param : params) {
			Variable v = (Variable) param;
			stringrep += " " + v.toString();
		}
		return stringrep;
	}

	public String toStringTyped() {
		String stringrep = name.toString();
		for (Object param : params) {
			Variable v = (Variable) param;
			stringrep += " " + v.toStringTyped();
		}
		return stringrep;
	}

	public abstract boolean effects(PredicateSymbol ps);

	protected abstract Action ground(Map varMap);

	public abstract Set getStaticConditionPredicates();

	public Action ground(List values) {
		Map varMap = new HashMap();
		Iterator vit = values.iterator();
		for (Object param : params) {
			Variable v = (Variable) param;
			PDDLObject o = (PDDLObject) vit.next();
			varMap.put(v, o);
		}
		Action a = this.ground(varMap);
		return a;


	}

	public Set ground(UngroundProblem up) {
		Set s = getParameterCombinations(up);
		Set rSet = new HashSet();
		for (Object o : s) {
			List l = (List) o;
			rSet.add(ground(l));
		}
		return rSet;
	}

	public Set getParameterCombinations(UngroundProblem up) {
		int arraysize = params.size();

		Set staticConditions = getStaticConditionPredicates();

		boolean[] set = new boolean[arraysize]; // which of the parameters has been fully set
		Arrays.fill(set, false);

		List combination = new ArrayList(arraysize);
		for (int i = 0; i < arraysize; ++i) {
			combination.add(null);
		}

		// Set for holding the combinations
		Set combinations = new HashSet();
		combinations.add(combination);

		// Loop through ones that must be static
		for (Object staticCondition : staticConditions) {
			Predicate p = (Predicate) staticCondition;

			Set newcombs = new HashSet();

			Set sp = (HashSet) up.staticPropositionMap.get(p.getPredicateSymbol());

			// Loop through those in the initial state
			for (Object value : sp) {
				Proposition prop = (Proposition) value;
				for (Object o : combinations) {
					ArrayList c = (ArrayList) o;
					// check its ok to put in
					boolean ok = true;
					Iterator propargit = prop.getParameters().iterator();
					int counter = 0;
					while (propargit.hasNext() && ok) {
						PDDLObject arg = (PDDLObject) propargit.next();
						Variable k = (Variable) p.getParameters().get(counter);
						int i = params.indexOf(k);
						if (i >= 0 && set[i]) {
							if (!c.get(i).equals(arg)) ok = false;
						}
						counter++;
					}
					//if so, duplicate it and put it in and put it in newcombs
					if (ok) {
						List sdup = (ArrayList) c.clone();
						counter = 0;
						propargit = prop.getParameters().iterator();
						while (propargit.hasNext()) {
							PDDLObject arg = (PDDLObject) propargit.next();
							Variable k = (Variable) p.getParameters().get(counter);
							int i = params.indexOf(k);
							if (i >= 0) {
								sdup.set(i, arg);
								counter++;
							}
						}
						newcombs.add(sdup);
					}
				}
			}

			combinations = newcombs;

			for (Object o : p.getParameters()) {
				Variable s = (Variable) o;
				int i = params.indexOf(s);

				if (i >= 0) set[i] = true;
			}
		}

		int counter = 0;
		for (Object param : params) {
			Variable p = (Variable) param;
			if (!set[counter]) {
				Set newcombs = new HashSet();
				for (Object o : combinations) {
					ArrayList s = (ArrayList) o;
					Set objs = (HashSet) up.typeSets.get(p.getType());
					for (Object obj : objs) {
						PDDLObject ob = (PDDLObject) obj;
						List sdup = (ArrayList) s.clone();
						sdup.set(counter, ob);
						newcombs.add(sdup);
					}
				}
				combinations = newcombs;

			}
			++counter;
		}
		return combinations;

	}
}
