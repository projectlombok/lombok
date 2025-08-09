//CONF: lombok.copyableAnnotations += test.lombok.NestedClassAndAnnotationNestedInImportedSibling$Other$OtherNested$TA
package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import test.lombok.NestedClassAndAnnotationNestedInImportedSibling.Other.OtherNested.TA;

public class NestedClassAndAnnotationNestedInImportedSibling {

	@RequiredArgsConstructor
	@Getter
	public static class Inner {
		@TA
		private final int someVal;
	}

	public static class Other {

		public static class OtherNested {
			@Retention(RetentionPolicy.RUNTIME)
			public @interface TA {}
		}
	}
}
