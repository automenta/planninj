package javaff.data.metric;

public class TotalTimeFunction extends NamedFunction {
	private static TotalTimeFunction t;

	private TotalTimeFunction() {
		super(new FunctionSymbol("total-time"));
	}

	public static TotalTimeFunction getInstance() {
		if (t == null) t = new TotalTimeFunction();
		return t;
	}
}
