//version 8:
//CONF: lombok.nonNull.exceptionType = Assertion

public class NonNullWithAssertion {
	@lombok.NonNull @lombok.Setter private String test;

	public void testMethod(@lombok.NonNull String arg) {
		System.out.println(arg);
	}

	public void testMethodWithIf(@lombok.NonNull String arg) {
		if (arg == null) throw new NullPointerException("Oops");
	}
}
