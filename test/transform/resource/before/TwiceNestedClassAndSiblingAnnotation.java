//CONF: lombok.copyableAnnotations += test.lombok.TwiceNestedClassAndSiblingAnnotation.Other$TA
package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class TwiceNestedClassAndSiblingAnnotation {

	public static class Other {

		@RequiredArgsConstructor
		@Getter
		public static class OtherInner {
			@TA
			private final int someVal;
		}

		@Retention(RetentionPolicy.RUNTIME)
		public @interface TA {}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TA {}
}
