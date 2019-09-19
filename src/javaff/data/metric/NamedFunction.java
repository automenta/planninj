package javaff.data.metric;

import javaff.data.strips.PDDLObject;
import javaff.data.strips.Variable;
import javaff.planning.MetricState;
import javaff.scheduling.MatrixSTN;

import java.math.BigDecimal;
import java.util.Map;

public class NamedFunction extends javaff.data.Literal implements Function {
	protected NamedFunction() {

	}

	public NamedFunction(FunctionSymbol fs) {
		super.setPredicateSymbol(fs);
	}

	public BigDecimal getValue(MetricState ms) {
		return ms.getValue(this);
	}

	public BigDecimal getMaxValue(MatrixSTN stn) {
		return getValue(null);
	}

	public BigDecimal getMinValue(MatrixSTN stn) {
		return getValue(null);
	}

	public boolean effectedBy(ResourceOperator ro) {
		return this.equals(ro.resource);
	}

	public Function replace(ResourceOperator ro) {
		if (ro.resource.equals(this)) {
			if (ro.type == MetricSymbolStore.INCREASE)
				return new BinaryFunction(MetricSymbolStore.PLUS, this, ro.change);
			else if (ro.type == MetricSymbolStore.DECREASE)
				return new BinaryFunction(MetricSymbolStore.MINUS, this, ro.change);
			else if (ro.type == MetricSymbolStore.SCALE_UP)
				return new BinaryFunction(MetricSymbolStore.MULTIPLY, this, ro.change);
			else if (ro.type == MetricSymbolStore.SCALE_DOWN)
				return new BinaryFunction(MetricSymbolStore.DIVIDE, this, ro.change);
			else if (ro.type == MetricSymbolStore.ASSIGN) return ro.change;
			else return this;
		} else return this;
	}

	public Function staticify(Map fValues) {
		if (isStatic()) {
			BigDecimal d = (BigDecimal) fValues.get(this);
			return new NumberFunction(d);
		} else return this;
	}

	public Function makeOnlyDurationDependent(MetricState s) {
		return new NumberFunction(getValue(s));
	}

	public Function ground(Map varMap) {
		NamedFunction nf = new NamedFunction((FunctionSymbol) name);
		for (Object parameter : parameters) {
			Variable v = (Variable) parameter;
			PDDLObject po = (PDDLObject) varMap.get(v);
			nf.addParameter(po);
		}
		return nf;
	}

	public String toString() {
		return "(" + super.toString() + ")";
	}

	public String toStringTyped() {
		return "(" + super.toStringTyped() + ")";
	}

	public int hashCode() {
		int hash = 7;
		hash = 31 * hash ^ name.hashCode();
		hash = 31 * hash ^ parameters.hashCode();
		return hash;
	}
}
