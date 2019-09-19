package javaff.data.metric;

import javaff.planning.MetricState;
import javaff.scheduling.MatrixSTN;

import java.math.BigDecimal;
import java.util.Map;

public interface Function {
	BigDecimal getValue(MetricState s);

	boolean isStatic();

	String toStringTyped();

	Function ground(Map varMap);

	Function staticify(Map fValues);

	boolean effectedBy(ResourceOperator ro);

	Function replace(ResourceOperator ro); //replaces the resource for the change

	Function makeOnlyDurationDependent(MetricState s);

	BigDecimal getMaxValue(MatrixSTN stn);

	BigDecimal getMinValue(MatrixSTN stn);
}
