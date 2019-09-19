package javaff.scheduling;

import javaff.data.*;
import javaff.data.metric.*;
import javaff.data.temporal.StartInstantAction;
import javaff.planning.TemporalMetricState;

import java.math.BigDecimal;
import java.util.*;

public class JavaFFScheduler implements Scheduler {
	protected final GroundProblem problem;

	public JavaFFScheduler(GroundProblem p) {
		problem = p;
	}

	public TimeStampedPlan schedule(TotalOrderPlan top) {
		PartialOrderPlan pop = GreedyPartialOrderLifter.lift(top, problem);

		MatrixSTN stn = new MatrixSTN(top);

		stn.addConstraints(pop.getTemporalConstraints());


		//Sort out the Durations
		Map states = new HashMap(); //Maps (Actions => states (which the actions are applied in))
		Iterator ait = top.getActions().iterator();
		TemporalMetricState state = problem.getTemporalMetricInitialState();
		while (ait.hasNext()) {
			Action a = (Action) ait.next();
			if (a instanceof StartInstantAction) {
				StartInstantAction sia = (StartInstantAction) a;
				List l = TemporalConstraint.getBounds(sia, sia.getSibling(), sia.parent.getMaxDuration(state), sia.parent.getMinDuration(state));
				stn.addConstraints(new HashSet(l));
			}
			states.put(a, state);
			state = (TemporalMetricState) state.apply(a);
		}


		stn.consistent();

		// sort out the resources
		Map graphs = new HashMap(); //Maps (NamedResources => PrecedenceGraphs)
		ait = top.getActions().iterator();
		while (ait.hasNext()) {
			Action a = (Action) ait.next();

			for (Object value : a.getComparators()) {
				//WARNING WARNING WARNING - assumes comparators are of the form (NamedFunction </>/<=/>= StaticFunction)
				BinaryComparator bc = (BinaryComparator) value;
				NamedFunction res = (NamedFunction) bc.first;
				PrecedenceResourceGraph prg = (PrecedenceResourceGraph) graphs.get(res);
				if (prg == null) {
					prg = new PrecedenceResourceGraph(stn);
					graphs.put(res, prg);
				}
				state = (TemporalMetricState) states.get(a);
				BigDecimal d = bc.second.getValue(state);
				prg.addCondition(new BinaryComparator(bc.type, res, new NumberFunction(d)), a);
			}

			for (Object o : a.getOperators()) {
				ResourceOperator ro = (ResourceOperator) o;
				NamedFunction res = ro.resource;
				PrecedenceResourceGraph prg = (PrecedenceResourceGraph) graphs.get(res);
				if (prg == null) {
					prg = new PrecedenceResourceGraph(stn);
					graphs.put(res, prg);
				}
				prg.addOperator(new ResourceOperator(ro.type, res, ro.change.makeOnlyDurationDependent(state)), a);
			}

		}


		for (Object o : graphs.keySet()) {
			NamedFunction nf = (NamedFunction) o;
			PrecedenceResourceGraph prg = (PrecedenceResourceGraph) graphs.get(nf);
			prg.addOperator(new ResourceOperator(MetricSymbolStore.INCREASE, nf, new NumberFunction(nf.getValue(problem.getTemporalMetricInitialState()))), stn.START);
			boolean changesMade = true;
			while (changesMade) {
				changesMade = prg.meetConditions();
				stn.constrain();
			}
			changesMade = true;
			while (changesMade) {
				changesMade = prg.limitBounds();
				stn.constrain();
			}

		}


		Metric m = problem.metric;
		if (m != null && m.func instanceof NamedFunction && !(m.func instanceof TotalTimeFunction)) {
			PrecedenceResourceGraph prg = (PrecedenceResourceGraph) graphs.get(m.func);
			if (m.type == Metric.MAXIMIZE) prg.maximize();
			else if (m.type == Metric.MINIMIZE) prg.minimize();
		}

		stn.constrain();

		stn.minimizeTime();
		stn.minimizeDuration();
		stn.constrain();

		TimeStampedPlan p = stn.getTimes();

		return p;
	}


}
