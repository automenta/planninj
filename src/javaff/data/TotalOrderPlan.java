package javaff.data;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

public class TotalOrderPlan implements Plan, Cloneable {
	private List plan = new ArrayList();

	public Object clone() {
		TotalOrderPlan rTOP = new TotalOrderPlan();
		rTOP.plan = (List) ((ArrayList) plan).clone();
		return rTOP;
	}

	public void addAction(Action a) {
		plan.add(a);
	}

	public int getPlanLength() {
		return plan.size();
	}

	public Iterator iterator() {
		return plan.iterator();
	}

	public ListIterator listIteratorEnd() {
		return plan.listIterator(plan.size());
	}

	public ListIterator listIterator(Action a) {
		return plan.listIterator(plan.indexOf(a));
	}

	public Set getActions() {
		return new HashSet(plan);
	}

	public boolean equals(Object obj) {
		if (obj instanceof TotalOrderPlan) {
			TotalOrderPlan p = (TotalOrderPlan) obj;
			return (plan.equals(p.plan));
		} else return false;
	}

	public int hashCode() {
		return plan.hashCode();
	}

	public void print(PrintStream ps) {
		for (Object o : plan) {
			ps.println("(" + o + ")");
		}
	}

	public void print(PrintWriter pw) {
		for (Object o : plan) {
			pw.println("(" + o + ")");
		}
	}
}
