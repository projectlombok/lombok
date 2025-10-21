//CONF: lombok.copyableAnnotations += test.lombok.other.Other$TA
package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import test.lombok.other.Other.TA;

public class NestedClassAndNestedAnnotationWithOverridingNestedAnnotationImport {

	@RequiredArgsConstructor
	@Getter
	public static class Inner {
		@TA private final int someVal;
	}

	public static class Other {

		@Retention(RetentionPolicy.RUNTIME)
		public @interface TA {}
	}
}
