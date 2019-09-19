package javaff.planning;

import javaff.data.GroundCondition;
import javaff.data.GroundProblem;
import javaff.data.metric.*;

import java.math.BigDecimal;
import java.util.*;

public class RelaxedMetricPlanningGraph extends RelaxedPlanningGraph {
	protected final Set metricGoal = new HashSet();

	protected List minResources = null;       //List of Maps [(PGFunction => BigDecimal)]
	protected List maxResources = null;

	protected final Map PGFuncMap = new HashMap();         // (NamedFunction => PGNamedFunction)
	protected final Map ActionComparators = new HashMap(); // (PGAction => (PGBinaryComparator))
	protected final Map ActionOperators = new HashMap();   // (PGAction => (PGFunctionOperator))

	//Used during graph construction
	private Set resOps = null;       // PGResourceOperators that are in the graph

	public RelaxedMetricPlanningGraph(GroundProblem gp) {
		super(gp);
		setupPGFuncMap(gp.functionValues.keySet());
		makeComparators(actions);
		makeOperators(actions);
		resOps = new HashSet();
	}

	//*************************************
	// Initial Setup
	//*************************************

	private void setupPGFuncMap(Set funcs) {
		for (Object func : funcs) {
			NamedFunction nf = (NamedFunction) func;
			PGFuncMap.put(nf, new PGNamedFunction(nf));
		}
	}

	private void makeComparators(Set actions) {
		for (Object action : actions) {
			PGAction pga = (PGAction) action;
			Set ss = new HashSet();
			ActionComparators.put(pga, ss);
			Set cs = pga.getComparators();
			for (Object c : cs) {
				BinaryComparator bc = (BinaryComparator) c;
				ss.add(makeComparator(bc));
			}
		}
	}

	private PGBinaryComparator makeComparator(BinaryComparator bc) {
		PGFunction f = makeFunction(bc.first);
		PGFunction s = makeFunction(bc.second);
		return new PGBinaryComparator(f, s, bc.type);
	}

	private void makeOperators(Set actions) {
		for (Object action : actions) {
			PGAction pga = (PGAction) action;
			Set s = new HashSet();
			ActionOperators.put(pga, s);
			Set os = pga.getOperators();
			for (Object o : os) {
				ResourceOperator ro = (ResourceOperator) o;
				s.add(makeOperator(ro));
			}
		}
	}

	private PGResourceOperator makeOperator(ResourceOperator ro) {
		PGNamedFunction r = (PGNamedFunction) PGFuncMap.get(ro.resource);
		PGFunction c = makeFunction(ro.change);
		return new PGResourceOperator(r, c, ro.type);
	}


	protected PGFunction makeFunction(Function f) {
		if (f instanceof NamedFunction) return (PGNamedFunction) PGFuncMap.get(f);
		else if (f instanceof NumberFunction) return new PGNumberFunction(f.getValue(null));
		else if (f instanceof BinaryFunction) {
			BinaryFunction bf = (BinaryFunction) f;
			PGFunction f1 = makeFunction(bf.first);
			PGFunction s2 = makeFunction(bf.second);
			return new PGBinaryFunction(f1, s2, bf.type);
		} else return null;
	}

	protected void setGoal(GroundCondition g) {
		super.setGoal(g);
		for (Object o : g.getComparators()) {
			BinaryComparator c = (BinaryComparator) o;
			metricGoal.add(makeComparator(c));
		}
	}


	//*************************************
	// Graph Setup
	//*************************************
	protected void resetAll(State s) {
		super.resetAll(s);
		maxResources = new ArrayList();
		minResources = new ArrayList();
		setInitialValues((MetricState) s);
	}

	protected void setInitialValues(MetricState ms) {
		Iterator fit = ms.funcValues.keySet().iterator();
		Map max = new HashMap();
		Map min = new HashMap();
		while (fit.hasNext()) {
			NamedFunction nf = (NamedFunction) fit.next();
			PGFunction pgf = (PGFunction) PGFuncMap.get(nf);
			BigDecimal bd = (BigDecimal) ms.funcValues.get(nf);
			max.put(pgf, bd);
			min.put(pgf, bd);
		}
		maxResources.add(max);
		minResources.add(min);
	}

	//*************************************
	// Graph Construction Methods
	//*************************************

	protected ArrayList createFactLayer(Set scheduledFacts, int layer) {
		ArrayList rActionList = super.createFactLayer(scheduledFacts, layer);
		updateResourceValues(layer);
		return rActionList;
	}

	private void updateResourceValues(int layer) {
		if (layer == 0) return;
		//duplicate the previous layers
		HashMap maxminus = (HashMap) maxResources.get(layer - 1);
		HashMap minminus = (HashMap) minResources.get(layer - 1);
		Map newmax = (HashMap) maxminus.clone();
		Map newmin = (HashMap) minminus.clone();
		maxResources.add(layer, newmax);
		minResources.add(layer, newmin);

		//loop throught the current resource operators and if they increase/decrease update the values on the new layer
		for (Object resOp : resOps) {
			PGResourceOperator ro = (PGResourceOperator) resOp;
			BigDecimal max = ro.resource.getMaxValue(layer - 1, maxResources, minResources);
			BigDecimal min = ro.resource.getMinValue(layer - 1, maxResources, minResources);
			BigDecimal nmax = ro.maximise(layer - 1, maxResources, minResources);
			BigDecimal nmin = ro.minimise(layer - 1, maxResources, minResources);
			if (nmax.compareTo(max) > 0) {
				newmax.put(ro.resource, nmax);
				numeric_level_off++;
			}
			if (nmin.compareTo(min) < 0) {
				newmin.put(ro.resource, nmin);
				numeric_level_off++;
			}
		}
	}

	protected HashSet filterSet(Set pActions, int layer) {
		Set fActions = super.filterSet(pActions, layer);
		HashSet rSet = new HashSet();
		for (Object fAction : fActions) {
			PGAction a = (PGAction) fAction;
			if (actionReady(a, layer)) rSet.add(a);
			else readyActions.add(a);
		}
		readyActions.removeAll(rSet);
		return rSet;
	}

	private boolean actionReady(PGAction a, int layer) {
		Set cs = (HashSet) ActionComparators.get(a);
		Iterator csit = cs.iterator();
		boolean allmet = true;
		while (csit.hasNext() && allmet) {
			PGBinaryComparator c = (PGBinaryComparator) csit.next();
			allmet = c.met(layer, maxResources, minResources);
		}
		return allmet;
	}

	protected HashSet calculateActionMutexesAndProps(Set filteredSet, int pLayer) {
		HashSet rSet = super.calculateActionMutexesAndProps(filteredSet, pLayer);
		for (Object o : filteredSet) {
			PGAction a = (PGAction) o;
			Set ops = (Set) ActionOperators.get(a);
			resOps.addAll(ops);
		}
		return rSet;
	}


	//*************************************
	// Graph Extraction
	//*************************************

	protected boolean goalMet() {
		boolean met = super.goalMet();
		Iterator mgit = metricGoal.iterator();
		while (met && mgit.hasNext()) {
			PGBinaryComparator c = (PGBinaryComparator) mgit.next();
			met = c.met(num_layers, maxResources, minResources);
		}
		return met;
	}

	public List extractPlan() //Should be done better buts its not
	{
		return searchRelaxedPlan(goal, metricGoal, num_layers);
	}

	public List searchRelaxedPlan(Set goalSet, Set mgoalSet, int l) {
		if (l == 0) return new ArrayList();
		Set chosenActions = new HashSet();
		//loop through actions to achieve the goal set
		for (Object element : goalSet) {
			PGProposition g = (PGProposition) element;
			PGAction a = null;
			for (Object o : g.achievedBy) {
				PGAction na = (PGAction) o;
				if (na.layer < l && na.layer >= 0) {
					if (na instanceof PGNoOp) {
						a = na;
						break;
					} else if (chosenActions.contains(na)) {
						a = na;
						break;
					} else {
						if (a == null) a = na;
						else if (a.difficulty > na.difficulty) a = na;
					}
				}
			}

			if (a != null) chosenActions.add(a);
		}

		Set newMGoalSet = new HashSet();
		//loop through the metric goals and see if they are satisfied, if not find some actions
		for (Object item : mgoalSet) {
			PGBinaryComparator c = (PGBinaryComparator) item;
			Iterator ait = actions.iterator();
			while (!c.met(l - 1, maxResources, minResources) && ait.hasNext()) {
				PGAction a = null;
				while (ait.hasNext()) {
					a = (PGAction) ait.next();
					if (a.layer < l && a.layer >= 0 && c.makeBetter(a)) break;
				}

				for (Object value : (Set) ActionOperators.get(a)) {
					PGResourceOperator o = (PGResourceOperator) value;
					if (c.makeBetter(o)) {
						c = c.affect(o);
					}
				}
				chosenActions.add(a);
			}
			newMGoalSet.add(c);
		}

		Set newGoalSet = new HashSet();
		//loop through chosen actions  adding in propositions and comparators
		for (Object chosenAction : chosenActions) {
			PGAction ca = (PGAction) chosenAction;
			newGoalSet.addAll(ca.conditions);
			newMGoalSet.addAll((Set) ActionComparators.get(ca));
		}

		List rplan = searchRelaxedPlan(newGoalSet, newMGoalSet, l - 1);
		rplan.addAll(chosenActions);
		return rplan;
	}


	//*************************************
	// Internal Metric Classes
	//*************************************

	// Note these do not deal with max and mins where there a -ve changes with multiplication and division. For example if x*y could be max if max*max or min*min if both mins were negative

	public void printLayer(int i) {
		super.printLayer(i);
		Map max = (HashMap) maxResources.get(i);
		Map min = (HashMap) minResources.get(i);
		System.out.println("Resources:");
		for (Object o : max.keySet()) {
			PGNamedFunction r = (PGNamedFunction) o;
			System.out.print("\t" + r.namedFunction.toString());
			System.out.print(" max:" + max.get(r));
			System.out.println(" min:" + min.get(r));
		}
	}

	protected interface PGFunction {
		BigDecimal getMaxValue(int layer, List maxes, List mins);

		BigDecimal getMinValue(int layer, List maxes, List mins);

		boolean effectedBy(PGResourceOperator ro);

		boolean increase(PGResourceOperator ro);

		boolean decrease(PGResourceOperator ro);
	}

	protected class PGNamedFunction implements PGFunction {
		public NamedFunction namedFunction;

		protected PGNamedFunction() {

		}

		public PGNamedFunction(NamedFunction nf) {
			namedFunction = nf;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins) {
			Map max = (HashMap) maxes.get(layer);
			return (BigDecimal) max.get(this);
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins) {
			Map min = (HashMap) mins.get(layer);
			return (BigDecimal) min.get(this);
		}

		public int hashcode() {
			return namedFunction.hashCode();
		}

		public boolean effectedBy(PGResourceOperator ro) {
			return (this == ro.resource);
		}

		public boolean increase(PGResourceOperator ro) {
			return ro.increase(this);
		}

		public boolean decrease(PGResourceOperator ro) {
			return ro.decrease(this);
		}
	}

	protected class PGNumberFunction implements PGFunction {
		public final BigDecimal value;

		public PGNumberFunction(BigDecimal v) {
			value = v;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins) {
			return value;
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins) {
			return value;
		}

		public boolean effectedBy(PGResourceOperator ro) {
			return false;
		}

		public boolean increase(PGResourceOperator ro) {
			return false;
		}

		public boolean decrease(PGResourceOperator ro) {
			return false;
		}
	}

	protected class PGBinaryFunction implements PGFunction {
		public final PGFunction first;
		public final PGFunction second;
		public final int type;

		public PGBinaryFunction(PGFunction f, PGFunction s, int t) {
			first = f;
			second = s;
			type = t;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins) {
			if (type == MetricSymbolStore.PLUS)
				return first.getMaxValue(layer, maxes, mins).add(second.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.MINUS)
				return first.getMaxValue(layer, maxes, mins).subtract(second.getMinValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.MULTIPLY)
				return first.getMaxValue(layer, maxes, mins).multiply(second.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.DIVIDE)
				return first.getMaxValue(layer, maxes, mins).divide(second.getMinValue(layer, maxes, mins), MetricSymbolStore.SCALE, MetricSymbolStore.ROUND);
			else return null;
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins) {
			return getMaxValue(layer, mins, maxes);
		}

		public boolean effectedBy(PGResourceOperator ro) {
			return (first.effectedBy(ro) || second.effectedBy(ro));
		}

		public boolean increase(PGResourceOperator ro) {
			if (type == MetricSymbolStore.PLUS) return (first.increase(ro) || second.increase(ro));
			else if (type == MetricSymbolStore.MINUS) return (first.increase(ro) || second.decrease(ro));
			else if (type == MetricSymbolStore.MULTIPLY) return (first.increase(ro) || second.increase(ro));
			else if (type == MetricSymbolStore.DIVIDE) return (first.increase(ro) || second.decrease(ro));
			else return false;
		}

		public boolean decrease(PGResourceOperator ro) {
			if (type == MetricSymbolStore.PLUS) return (first.decrease(ro) || second.decrease(ro));
			else if (type == MetricSymbolStore.MINUS) return (first.decrease(ro) || second.increase(ro));
			else if (type == MetricSymbolStore.MULTIPLY) return (first.decrease(ro) || second.decrease(ro));
			else if (type == MetricSymbolStore.DIVIDE) return (first.decrease(ro) || second.increase(ro));
			else return false;
		}
	}

	protected class PGResourceOperator {
		public final PGNamedFunction resource;
		public final PGFunction change;
		public final int type;

		public PGResourceOperator(PGNamedFunction r, PGFunction c, int t) {
			resource = r;
			change = c;
			type = t;
		}

		public BigDecimal maximise(int layer, List maxes, List mins) {
			if (type == MetricSymbolStore.ASSIGN) return change.getMaxValue(layer, maxes, mins);
			else if (type == MetricSymbolStore.INCREASE)
				return resource.getMaxValue(layer, maxes, mins).add(change.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.DECREASE)
				return resource.getMaxValue(layer, maxes, mins).subtract(change.getMinValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.SCALE_UP)
				return resource.getMaxValue(layer, maxes, mins).multiply(change.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.SCALE_DOWN)
				return resource.getMaxValue(layer, maxes, mins).divide(change.getMinValue(layer, maxes, mins), MetricSymbolStore.SCALE, MetricSymbolStore.ROUND);
			else return null;
		}

		public BigDecimal minimise(int layer, List maxes, List mins) {
			return maximise(layer, mins, maxes);
		}

		public PGFunction invertFunction(PGFunction f) {
			if (type == MetricSymbolStore.ASSIGN) return change;
			else if (type == MetricSymbolStore.INCREASE)
				return new PGBinaryFunction(f, change, MetricSymbolStore.MINUS);
			else if (type == MetricSymbolStore.DECREASE) return new PGBinaryFunction(f, change, MetricSymbolStore.PLUS);
			else if (type == MetricSymbolStore.SCALE_UP)
				return new PGBinaryFunction(f, change, MetricSymbolStore.DIVIDE);
			else if (type == MetricSymbolStore.SCALE_DOWN)
				return new PGBinaryFunction(f, change, MetricSymbolStore.MULTIPLY);
			else return null;
		}

		public boolean increase(PGFunction f) {
			if (resource == f) {
				if (type == MetricSymbolStore.ASSIGN)
					return true; // could do something better here by looking at the values
				else if (type == MetricSymbolStore.INCREASE) return true;
				else if (type == MetricSymbolStore.DECREASE) return false;
				else if (type == MetricSymbolStore.SCALE_UP) return true;
				else if (type == MetricSymbolStore.SCALE_DOWN) return false;
				else return false;
			} else return false;
		}

		public boolean decrease(PGFunction f) {
			if (resource == f) {
				if (type == MetricSymbolStore.ASSIGN)
					return true; // could do something better here by looking at the values
				else if (type == MetricSymbolStore.INCREASE) return false;
				else if (type == MetricSymbolStore.DECREASE) return true;
				else if (type == MetricSymbolStore.SCALE_UP) return false;
				else return type == MetricSymbolStore.SCALE_DOWN;
			} else return false;
		}
	}

	//*************************************
	// Debugging Classes
	//*************************************

	protected class PGBinaryComparator {
		public final PGFunction left;
		public final PGFunction right;
		public final int type;

		public PGBinaryComparator(PGFunction l, PGFunction r, int t) {
			left = l;
			right = r;
			type = t;
		}

		public boolean met(int layer, List maxes, List mins) {
			if (type == MetricSymbolStore.GREATER_THAN)
				return left.getMaxValue(layer, maxes, mins).compareTo(right.getMinValue(layer, maxes, mins)) > 0;
			else if (type == MetricSymbolStore.GREATER_THAN_EQUAL)
				return left.getMaxValue(layer, maxes, mins).compareTo(right.getMinValue(layer, maxes, mins)) >= 0;
			else if (type == MetricSymbolStore.LESS_THAN)
				return left.getMinValue(layer, maxes, mins).compareTo(right.getMaxValue(layer, maxes, mins)) < 0;
			else if (type == MetricSymbolStore.LESS_THAN_EQUAL)
				return left.getMinValue(layer, maxes, mins).compareTo(right.getMaxValue(layer, maxes, mins)) <= 0;
			else if (type == MetricSymbolStore.EQUAL)
				return (left.getMaxValue(layer, maxes, mins).compareTo(right.getMinValue(layer, maxes, mins)) >= 0 && left.getMinValue(layer, maxes, mins).compareTo(right.getMaxValue(layer, maxes, mins)) <= 0);
			else return true;
		}


		public boolean effectedBy(PGResourceOperator ro) {
			return (left.effectedBy(ro) || right.effectedBy(ro));
		}

		public PGBinaryComparator affect(PGResourceOperator ro) {
			if (effectedBy(ro)) {
				PGFunction newLeft = left;
				PGFunction newRight = right;
				if (left.effectedBy(ro)) {
					newLeft = ro.invertFunction(left);
				}
				if (right.effectedBy(ro)) {
					newRight = ro.invertFunction(right);
				}
				PGBinaryComparator nbc = new PGBinaryComparator(newLeft, newRight, type);
				return nbc;
			} else return this;
		}

		public boolean makeBetter(PGResourceOperator ro) {
			if (type == MetricSymbolStore.GREATER_THAN || type == MetricSymbolStore.GREATER_THAN_EQUAL)
				return left.increase(ro) || right.decrease(ro);
			else if (type == MetricSymbolStore.LESS_THAN || type == MetricSymbolStore.LESS_THAN_EQUAL)
				return left.decrease(ro) || right.increase(ro);
			else if (type == MetricSymbolStore.EQUAL) return true;
			else return true;
		}

		public boolean makeBetter(PGAction a) {
			for (Object value : (Set) ActionOperators.get(a)) {
				PGResourceOperator o = (PGResourceOperator) value;
				if (makeBetter(o)) return true;
			}
			return false;
		}
	}

}