//version 7:
//CONF: lombok.nonNull.exceptionType = Jdk
import static java.util.Objects.*;
public class NonNullWithJdk {
	@lombok.NonNull @lombok.Setter private String test;

	public void testMethod(@lombok.NonNull String arg) {
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
		requireNonNull("");
	}

	public void testMethodWithFakeCheck2(@lombok.NonNull String arg) {
		java.util.Objects.requireNonNull(test);
	}
	
	public void testMethodWithFakeCheckAssign(@lombok.NonNull String arg) {
		test = requireNonNull(test);
	}
}
