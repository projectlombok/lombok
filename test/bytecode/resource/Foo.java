public class Foo implements java.util.RandomAccess {
	private static final long LONG = 123L;
	private static final long LONG_OVERFLOW = 0x1FFFFFFFFL;
	private static final String ONE = "E\u00e9n";
	private static final int INT = 123;
	private static final double DOUBLE = 1.23;
	private static final double DOUBLE_NAN = Double.NaN;
	private static final double DOUBLE_INF = Double.POSITIVE_INFINITY;
	private static final double DOUBLE_NEG_INF = Double.NEGATIVE_INFINITY;
	
	private static final float FLOAT = 1.23F;
	private static final float FLOAT_NAN = Float.NaN;
	private static final float FLOAT_INF = Float.POSITIVE_INFINITY;
	private static final float FLOAT_NEG_INF = Float.NEGATIVE_INFINITY;
	
	{
		String value = toString();
		System.out.print(value);
		System.out.print("Two" + "Four");
	}
}