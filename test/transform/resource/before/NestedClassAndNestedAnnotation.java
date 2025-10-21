//CONF: lombok.copyableAnnotations += test.lombok.NestedClassAndNestedAnnotation$TA
package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class NestedClassAndNestedAnnotation {

	@RequiredArgsConstructor
	@Getter
	public static class Inner {
		@TA
		private final int someVal;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TA {}
}
