package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NestedClassAndAnnotationNestedInNonImportedSibling {

	public static class Inner {
		@Other.OtherNested.TA
		private final int someVal;

		@java.lang.SuppressWarnings("all")
		public Inner(@Other.OtherNested.TA final int someVal) {
			this.someVal = someVal;
		}

		@Other.OtherNested.TA
		@java.lang.SuppressWarnings("all")
		public int getSomeVal() {
			return this.someVal;
		}
	}

	public static class Other {

		public static class OtherNested {
			@Retention(RetentionPolicy.RUNTIME)
			public @interface TA {
			}
		}
	}
}
