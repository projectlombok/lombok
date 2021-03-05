//version 8:
//CONF: lombok.nonNull.exceptionType = Guava
import static com.google.common.base.Preconditions.*;
public class NonNullWithGuava {
	@lombok.NonNull @lombok.Setter private String test;

	public void testMethod(@lombok.NonNull String arg) {
		System.out.println(arg);
	}

	public void testMethodWithCheck1(@lombok.NonNull String arg) {
		checkNotNull(arg);
	}

	public void testMethodWithCheckAssign(@lombok.NonNull String arg) {
		test = checkNotNull(arg);
	}

	public void testMethodWithCheck2(@lombok.NonNull String arg) {
		com.google.common.base.Preconditions.checkNotNull(arg);
	}

	public void testMethodWithFakeCheck1(@lombok.NonNull String arg) {
		checkNotNull("");
	}

	public void testMethodWithFakeCheck2(@lombok.NonNull String arg) {
		com.google.common.base.Preconditions.checkNotNull(test);
	}
	
	public void testMethodWithFakeCheckAssign(@lombok.NonNull String arg) {
		test = checkNotNull(test);
	}
}
