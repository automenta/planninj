package javaff.data.metric;

import javaff.planning.MetricState;
import javaff.scheduling.MatrixSTN;

import java.math.BigDecimal;
import java.util.Map;

public class NumberFunction implements Function {
	final protected BigDecimal num;

	public NumberFunction(double d) {
		num = new BigDecimal(d);
	}

	public NumberFunction(BigDecimal d) {
		num = d;
	}

	public BigDecimal getValue(MetricState s) {
		return num;
	}

	public BigDecimal getMaxValue(MatrixSTN stn) {
		return getValue(null);
	}

	public BigDecimal getMinValue(MatrixSTN stn) {
		return getValue(null);
	}

	public Function staticify(Map fValues) {
		return this;
	}

	public Function makeOnlyDurationDependent(MetricState s) {
		return this;
	}

	public String toString() {
		return num.toString();
	}

	public String toStringTyped() {
		return toString();
	}

	public boolean isStatic() {
		return true;
	}

	public boolean effectedBy(ResourceOperator ro) {
		return false;
	}

	public Function replace(ResourceOperator ro) {
		return this;
	}

	public Function ground(Map varMap) {
		return this;
	}

	public boolean equals(Object obj) {
		if (obj instanceof NumberFunction) {
			NumberFunction nf = (NumberFunction) obj;
			return num.equals(nf.num);
		} else return false;
	}
}
