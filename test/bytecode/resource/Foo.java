public class Foo implements java.util.RandomAccess {
	private static final String ONE = "Eén";
	
	{
		String value = toString();
		System.out.print(value);
		System.out.print("Two" + "Four");
	}
}