package javaff.data;

import javaff.data.metric.NamedFunction;
import javaff.data.strips.Operator;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

public abstract class PDDLPrinter {
	public static void printToString(PDDLPrintable p, PrintStream ps, boolean typed, boolean bracketed, int indent) {
		printIndent(ps, indent);
		printToString(p, ps, typed, bracketed);
	}

	public static void printToString(PDDLPrintable p, PrintStream ps, boolean typed, boolean bracketed) {
		if (bracketed) ps.print("(");
		if (typed) ps.print(p.toStringTyped());
		else ps.print(p.toString());
		if (bracketed) ps.print(")");
	}


	public static void printIndent(PrintStream ps, int indent) {
		for (int i = 0; i < indent; ++i) {
			ps.print("\t");
		}
	}

	public static void printToString(Collection c, PrintStream ps, boolean typed, boolean bracketed, int indent) {
		Iterator it = c.iterator();
		while (it.hasNext()) {
			printIndent(ps, indent);
			PDDLPrintable p = (PDDLPrintable) it.next();
			printToString(p, ps, typed, bracketed);
			if (it.hasNext()) ps.println();
		}
	}

	public static void printToString(Collection c, PrintStream ps, boolean typed, boolean bracketed) {
		printToString(c, ps, typed, bracketed, 0);
	}

	public static void printToString(Collection c, String label, PrintStream ps, boolean typed, boolean bracketed, int indent) {
		ps.println();
		printIndent(ps, indent);
		ps.print("(");
		ps.println(label);
		printToString(c, ps, typed, bracketed, indent + 1);
		ps.print(")");
	}

	public static void printToString(Collection c, String label, PrintStream ps, boolean typed, boolean bracketed) {
		printToString(c, label, ps, typed, bracketed, 0);
	}

	public static void printDomainFile(UngroundProblem p, java.io.PrintStream pstream) {
		pstream.println("(define (domain " + p.DomainName + ")");

		pstream.print("\t(:requirements");
		Iterator it = p.requirements.iterator();
		while (it.hasNext()) {
			pstream.print(" " + it.next());
		}
		pstream.print(")");

		printToString(p.types, ":types", pstream, true, false, 1);
		if (!p.constants.isEmpty()) printToString(p.constants, ":constants", pstream, true, false, 1);
		printToString(p.predSymbols, ":predicates", pstream, true, true, 1);
		if (!p.funcSymbols.isEmpty()) printToString(p.funcSymbols, ":functions", pstream, true, true, 1);

		it = p.actions.iterator();
		while (it.hasNext()) {
			pstream.println();
			Operator o = (Operator) it.next();
			o.PDDLPrint(pstream, 1);
		}


		pstream.println(")");
	}

	public static void printProblemFile(UngroundProblem p, java.io.PrintStream pstream) {
		pstream.println("(define (problem " + p.ProblemName + ")");
		pstream.print("\t(:domain " + p.ProblemDomainName + ")");

		printToString(p.objects, ":objects", pstream, true, false, 1);
		pstream.println();
		printIndent(pstream, 1);
		pstream.print("(");
		pstream.println(":init");
		printToString(p.initial, pstream, false, true, 2);
		for (Object o : p.funcValues.keySet()) {
			NamedFunction nf = (NamedFunction) o;
			pstream.println();
			printIndent(pstream, 2);
			pstream.print("(= ");
			printToString(nf, pstream, false, false);
			pstream.print(" " + p.funcValues.get(nf) + ")");
		}
		pstream.print(")");

		pstream.print("\n\t(:goal ");
		p.goal.PDDLPrint(pstream, 2);
		pstream.println(")");
		printIndent(pstream, 1);
		if (p.metric != null) p.metric.PDDLPrint(pstream, 1);
		pstream.print(")");
	}
}
