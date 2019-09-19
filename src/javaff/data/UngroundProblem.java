package javaff.data;

import javaff.data.metric.FunctionSymbol;
import javaff.data.strips.*;

import java.util.*;

public class UngroundProblem {
	public String DomainName;                               // Name of Domain
	public String ProblemName;                              // Name of Problem
	public String ProblemDomainName;                        // Name of Domain as specified by the Problem

	public final Set requirements = new HashSet();                // Requirements of the domain     (String)

	public final Set types = new HashSet();                       // For simple object types in this domain       (SimpleTypes)
	public final Map typeMap = new HashMap();                   // Set for mapping String -> types  (String => Type)
	public final Map typeSets = new HashMap();                 // Maps a type on to a set of PDDLObjects (Type => Set (PDDLObjects))

	public final Set predSymbols = new HashSet();                 // Set of all (ungrounded) predicate     (PredicateSymbol)
	public final Map predSymbolMap = new HashMap();             // Maps Strings of the symbol to the Symbols (String => PredicateSymbol)

	public final Set constants = new HashSet();                   // Set of all constant           (PDDLObjects)
	public final Map constantMap = new HashMap();               // Maps Strings of the constant to the PDDLObject

	public final Set funcSymbols = new HashSet();                 // Set of all function symbols (FunctionSymbol)
	public final Map funcSymbolMap = new HashMap();             // Maps Strings onto the Symbols (String => FunctionSymbol)

	public final Set actions = new HashSet();                     // List of all (ungrounded) actions      (Operators)

	public final Set objects = new HashSet();                     // Objects in the problem        (PDDLObject)
	public final Map objectMap = new HashMap();                // Maps Strings onto PDDLObjects (String => PDDLObject)

	public final Set initial = new HashSet();                     // Set of initial facts          (Proposition)
	public final Map funcValues = new HashMap();                // Maps functions onto numbers (NamedFunction => BigDecimal)
	public GroundCondition goal;

	public Metric metric;

	public final Map staticPropositionMap = new HashMap();      // (PredicateName => Set (Proposition))

	public UngroundProblem() {
		typeMap.put(SimpleType.rootType.toString(), SimpleType.rootType);
	}

	public GroundProblem ground() {
		calculateStatics();
		makeStaticPropositionMap();
		buildTypeSets();
		Set groundActions = new HashSet();
		for (Object action : actions) {
			Operator o = (Operator) action;
			Set s = o.ground(this);
			groundActions.addAll(s);
		}

		//static-ify the functions
		for (Object groundAction : groundActions) {
			Action a = (Action) groundAction;
			a.staticify(funcValues);
		}

		//remove static functions from the intial state
		removeStaticsFromInitialState();

		//-could put in code here to
		// a) get rid of static functions in initial state - DONE
		// b) get rid of static predicates in initial state - DONE
		// c) get rid of static propositions in the actions (this may have already been done)
		// d) get rid of no use actions (i.e. whose preconditions can't be achieved) 

		GroundProblem rGP = new GroundProblem(groundActions, initial, goal, funcValues, metric);
		return rGP;
	}

	private void buildTypeSets() // builds typeSets for easy access of all the objects of a particular type
	{
		for (Object type : types) {
			SimpleType st = (SimpleType) type;
			Set s = new HashSet();
			typeSets.put(st, s);

			for (Object object : objects) {
				PDDLObject o = (PDDLObject) object;
				if (o.isOfType(st)) s.add(o);
			}

			for (Object constant : constants) {
				PDDLObject c = (PDDLObject) constant;
				if (c.isOfType(st)) s.add(c);
			}
		}

		Set s = new HashSet(objects);
		s.addAll(constants);
		typeSets.put(SimpleType.rootType, s);
	}

	private void calculateStatics() // Determines whether the predicateSymbols and funcSymbols are static or not
	{
		for (Object predSymbol : predSymbols) {
			boolean isStatic = true;
			PredicateSymbol ps = (PredicateSymbol) predSymbol;
			Iterator oit = actions.iterator();
			while (oit.hasNext() && isStatic) {
				Operator o = (Operator) oit.next();
				isStatic = !o.effects(ps);
			}
			ps.setStatic(isStatic);
		}

		for (Object funcSymbol : funcSymbols) {
			boolean isStatic = true;
			FunctionSymbol fs = (FunctionSymbol) funcSymbol;
			Iterator oit = actions.iterator();
			while (oit.hasNext() && isStatic) {
				Operator o = (Operator) oit.next();
				isStatic = !o.effects(fs);
			}
			fs.setStatic(isStatic);
		}
	}

	private void makeStaticPropositionMap() {
		for (Object predSymbol : predSymbols) {
			PredicateSymbol ps = (PredicateSymbol) predSymbol;
			if (ps.isStatic()) {
				staticPropositionMap.put(ps, new HashSet());
			}
		}

		for (Object o : initial) {
			Proposition p = (Proposition) o;
			if (p.name.isStatic()) {
				Set pset = (Set) staticPropositionMap.get(p.name);
				pset.add(p);
			}
		}
	}

	private void removeStaticsFromInitialState() {
		//remove static functions
	  /*
  	Iterator fit = funcValues.keySet().iterator();
    Set staticFuncs = new HashSet();
    while (fit.hasNext())
    {
    	NamedFunction nf = (NamedFunction) fit.next();
      if (nf.isStatic()) staticFuncs.add(nf);
    }
    fit = staticFuncs.iterator();
    while (fit.hasNext())
    {
    	Object o = fit.next();
      funcValues.remove(o);
    }*/

		//remove static Propositions
		Iterator init = initial.iterator();
		Set staticProps = new HashSet();
		while (init.hasNext()) {
			Proposition p = (Proposition) init.next();
			if (p.isStatic()) staticProps.add(p);
		}
		initial.removeAll(staticProps);
	}

}