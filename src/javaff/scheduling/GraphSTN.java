package javaff.scheduling;

import javaff.data.strips.InstantAction;

import java.math.BigDecimal;
import java.util.*;

public class GraphSTN implements Cloneable, SimpleTemporalNetwork {
	final Set nodes = new HashSet();
	final Set edges = new HashSet();

	public Object clone() {
		GraphSTN newSTN = new GraphSTN();
		newSTN.nodes.addAll(this.nodes);
		newSTN.edges.addAll(this.edges);
		return newSTN;
	}

	public boolean consistent() {
		boolean consistency = true;
		Iterator sit = nodes.iterator();
		while (sit.hasNext() && consistency) {
			InstantAction source = (InstantAction) sit.next();
			consistency = consistentSource(source);
		}
		return consistency;
	}

	//Implementation of Bellman-Ford Algorithm for Single Source Shortest Path with negative edges
	public boolean consistentSource(InstantAction source) {
		List nodeIndex = new ArrayList(nodes);
		InstantAction[] p = new InstantAction[nodeIndex.size()];
		BigDecimal[] d = new BigDecimal[nodeIndex.size()];
		for (int count = 0; count < nodeIndex.size(); ++count) {
			p[count] = (InstantAction) nodeIndex.get(count);
			d[count] = javaff.JavaFF.MAX_DURATION;
		}
		d[nodeIndex.indexOf(source)] = new BigDecimal(0);

		for (int count = 0; count < nodeIndex.size(); ++count) {
			for (Object edge : edges) {
				TemporalConstraint e = (TemporalConstraint) edge;
				//relax
				int sourceIndex = nodeIndex.indexOf(e.y);
				int sinkIndex = nodeIndex.indexOf(e.x);
				BigDecimal x = e.b.add(d[sourceIndex]);
				if (x.compareTo(d[sinkIndex]) < 0) {
					d[sinkIndex] = x;
					p[sinkIndex] = e.y;
				}
			}
		}

		for (Object edge : edges) {
			TemporalConstraint e = (TemporalConstraint) edge;
			if (e.b.add(d[nodeIndex.indexOf(e.y)]).compareTo(d[nodeIndex.indexOf(e.x)]) < 0) return false;
		}

		return true;
	}

	public void addConstraints(Set constraints) {
		for (Object constraint : constraints) {
			TemporalConstraint c = (TemporalConstraint) constraint;
			addConstraint(c);
		}
	}

	public void addConstraint(TemporalConstraint c) {
		nodes.add(c.y);
		nodes.add(c.x);
		edges.add(c);
	}


	private static class Node {
		InstantAction a;
		BigDecimal d;
		Node p;

		public Node(InstantAction act) {
			act = a;
		}

		public int hashCode() {
			return a.hashCode();
		}
	}

}







