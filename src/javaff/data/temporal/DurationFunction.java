package javaff.data.temporal;

import javaff.data.metric.Function;
import javaff.data.metric.NamedFunction;
import javaff.data.metric.NumberFunction;
import javaff.planning.MetricState;
import javaff.scheduling.MatrixSTN;

import java.math.BigDecimal;
import java.util.Map;

public class DurationFunction extends NamedFunction {
	public DurativeAction durativeAction;
	UngroundDurativeAction ungroundDurativeAction;        //horrible hack

	public DurationFunction(DurativeAction da) {
		durativeAction = da;
	}

	public DurationFunction(UngroundDurativeAction uda) {
		ungroundDurativeAction = uda;
	}

	public BigDecimal getValue(MetricState ms) {
		return durativeAction.getDuration(ms);
	}

	public BigDecimal getMaxValue(MatrixSTN stn) {
		return stn.getMaximum(durativeAction);
	}

	public BigDecimal getMinValue(MatrixSTN stn) {
		return stn.getMinimum(durativeAction);
	}

	public Function staticify(Map fValues) {
		if (durativeAction.staticDuration()) {
			BigDecimal d = getValue(null);
			return new NumberFunction(d);
		} else return this;
	}

	public Function makeOnlyDurationDependent(MetricState s) {
		return this;
	}

	public Function ground(Map varMap) {
		return (Function) varMap.get(this);
	}


	public String toString() {
		return "?duration";
	}

	public String toStringTyped() {
		return "?duration";
	}

	public int hashCode() {
		int hash = 7;
		if (durativeAction != null) hash = 31 * hash ^ durativeAction.hashCode();
		else hash = 31 * hash ^ ungroundDurativeAction.hashCode();
		return hash;
	}

	public boolean equals(Object obj) {
		if (obj instanceof DurationFunction) {
			DurationFunction f = (DurationFunction) obj;
			if (f.durativeAction != null && durativeAction != null) return durativeAction.equals(f.durativeAction);
			else if (f.ungroundDurativeAction != null && ungroundDurativeAction != null)
				return ungroundDurativeAction.equals(f.ungroundDurativeAction);
			else return false;
		} else return false;
	}

}
