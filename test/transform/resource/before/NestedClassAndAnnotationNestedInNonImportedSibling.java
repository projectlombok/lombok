//CONF: lombok.copyableAnnotations += test.lombok.NestedClassAndAnnotationNestedInNonImportedSibling$Other$OtherNested$TA
package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class NestedClassAndAnnotationNestedInNonImportedSibling {

	@RequiredArgsConstructor
	@Getter
	public static class Inner {
		@Other.OtherNested.TA
		private final int someVal;
	}

	public static class Other {

		public static class OtherNested {
			@Retention(RetentionPolicy.RUNTIME)
			public @interface TA {}
		}
	}
}
