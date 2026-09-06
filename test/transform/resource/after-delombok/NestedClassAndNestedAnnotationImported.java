package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import test.lombok.NestedClassAndNestedAnnotationImported.Other.OtherNested.TA;

public class NestedClassAndNestedAnnotationImported {

	public static class Inner {
		@TA
		private final int someVal;

		@java.lang.SuppressWarnings("all")
		public Inner(@TA final int someVal) {
			this.someVal = someVal;
		}

		@TA
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
