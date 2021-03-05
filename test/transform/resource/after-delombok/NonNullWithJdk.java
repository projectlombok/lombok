//version 8:
import static java.util.Objects.*;
public class NonNullWithJdk {
	@lombok.NonNull
	private String test;
	public void testMethod(@lombok.NonNull String arg) {
		java.util.Objects.requireNonNull(arg, "arg is marked non-null but is null");
		System.out.println(arg);
	}
	public void testMethodWithCheck1(@lombok.NonNull String arg) {
		requireNonNull(arg);
	}
	public void testMethodWithCheckAssign(@lombok.NonNull String arg) {
		test = requireNonNull(arg);
	}
	public void testMethodWithCheck2(@lombok.NonNull String arg) {
		java.util.Objects.requireNonNull(arg);
	}
	public void testMethodWithFakeCheck1(@lombok.NonNull String arg) {
		java.util.Objects.requireNonNull(arg, "arg is marked non-null but is null");
		requireNonNull("");
	}
	public void testMethodWithFakeCheck2(@lombok.NonNull String arg) {
		java.util.Objects.requireNonNull(arg, "arg is marked non-null but is null");
		java.util.Objects.requireNonNull(test);
	}
	public void testMethodWithFakeCheckAssign(@lombok.NonNull String arg) {
		java.util.Objects.requireNonNull(arg, "arg is marked non-null but is null");
		test = requireNonNull(test);
	}
	@java.lang.SuppressWarnings("all")
	public void setTest(@lombok.NonNull final String test) {
		java.util.Objects.requireNonNull(test, "test is marked non-null but is null");
		this.test = test;
	}
}