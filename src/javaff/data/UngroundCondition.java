package javaff.data;

import java.util.Map;
import java.util.Set;

public interface UngroundCondition extends Condition {
	GroundCondition groundCondition(Map varMap);

	Set getStaticPredicates();

	UngroundCondition minus(UngroundEffect effect);
}
