package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TwiceNestedClassAndSiblingAnnotation {

	public static class Other {

		public static class OtherInner {
			@TA
			private final int someVal;

			@java.lang.SuppressWarnings("all")
			public OtherInner(@TA final int someVal) {
				this.someVal = someVal;
			}

			@TA
			@java.lang.SuppressWarnings("all")
			public int getSomeVal() {
				return this.someVal;
			}
		}

		@Retention(RetentionPolicy.RUNTIME)
		public @interface TA {
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TA {
	}
}
