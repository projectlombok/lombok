//CONF: lombok.copyableAnnotations += test.lombok.NestedClassAndNestedAnnotationWithConflictingNestedImport$Other$TA
package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import test.lombok.other.Other;

public class NestedClassAndNestedAnnotationWithConflictingNestedImport {

	@RequiredArgsConstructor
	@Getter
	public static class Inner {
		@Other.TA private final int someVal;
	}

	public static class Other {

		@Retention(RetentionPolicy.RUNTIME)
		public @interface TA {}
	}
}
