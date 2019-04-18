public class NonNullWithAssertion {
	@lombok.NonNull
	private String test;
	public void testMethod(@lombok.NonNull String arg) {
		assert arg != null : "arg is marked non-null but is null";
		System.out.println(arg);
	}
	public void testMethodWithIf(@lombok.NonNull String arg) {
		if (arg == null) throw new NullPointerException("Oops");
	}	
	@java.lang.SuppressWarnings("all")
	public void setTest(@lombok.NonNull final String test) {
		assert test != null : "test is marked non-null but is null";
		this.test = test;
	}
}