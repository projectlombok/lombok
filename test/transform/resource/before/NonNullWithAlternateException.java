//CONF: lombok.nonNull.exceptionType = java.lang.IllegalArgumentException

public class NonNullWithAlternateException {
	@lombok.NonNull @lombok.Setter private String test;

	public void testMethod(@lombok.NonNull String arg) {
		System.out.println(arg);
	}
}
