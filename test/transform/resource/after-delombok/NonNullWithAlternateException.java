//version 8:
public class NonNullWithAlternateException {
	@lombok.NonNull
	private String test;
	public void testMethod(@lombok.NonNull String arg) {
		if (arg == null) {
			throw new java.lang.IllegalArgumentException("arg is marked non-null but is null");
		}
		System.out.println(arg);
	}
	@java.lang.SuppressWarnings("all")
	public void setTest(@lombok.NonNull final String test) {
		if (test == null) {
			throw new java.lang.IllegalArgumentException("test is marked non-null but is null");
		}
		this.test = test;
	}
}
