package javaff.data.metric;

import java.math.BigDecimal;

public abstract class MetricSymbolStore {
	public static final int GREATER_THAN = 0;
	public static final int GREATER_THAN_EQUAL = 1;
	public static final int LESS_THAN = 2;
	public static final int LESS_THAN_EQUAL = 3;
	public static final int EQUAL = 4;

	public static final int PLUS = 5;
	public static final int MINUS = 6;
	public static final int MULTIPLY = 7;
	public static final int DIVIDE = 8;

	public static final int ASSIGN = 9;
	public static final int INCREASE = 10;
	public static final int DECREASE = 11;
	public static final int SCALE_UP = 12;
	public static final int SCALE_DOWN = 13;

	public static final int SCALE = 2;
	public static final int ROUND = BigDecimal.ROUND_HALF_EVEN;

	public static int getType(String s) {
		switch (s) {
			case ">":
				return GREATER_THAN;
			case ">=":
				return GREATER_THAN_EQUAL;
			case "<":
				return LESS_THAN;
			case "<=":
				return LESS_THAN_EQUAL;
			case "=":
				return EQUAL;
			case "+":
				return PLUS;
			case "-":
				return MINUS;
			case "*":
				return MULTIPLY;
			case "/":
				return DIVIDE;
			case "assign":
			case ":=":
				return ASSIGN;
			case "increase":
			case "+=":
				return INCREASE;
			case "decrease":
			case "-=":
				return DECREASE;
			case "scale-up":
			case "*=":
				return SCALE_UP;
			case "scale-down":
			case "/=":
				return SCALE_DOWN;
			default:
				return -1;
		}
	}

	public static String getSymbol(int t) {
		switch (t) {
			case 0:
				return ">";
			case 1:
				return ">=";
			case 2:
				return "<";
			case 3:
				return "<=";
			case 4:
				return "=";
			case 5:
				return "+";
			case 6:
				return "-";
			case 7:
				return "*";
			case 8:
				return "/";
			case 9:
				return "assign";
			case 10:
				return "increase";
			case 11:
				return "decrease";
			case 12:
				return "scale-up";
			case 13:
				return "scale-down";
		}
		return "";
	}
}
