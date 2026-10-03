package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import test.lombok.other.Other;

public class NestedClassAndNestedAnnotationWithConflictingNestedImport {

	public static class Inner {
		@Other.TA
		private final int someVal;

		@java.lang.SuppressWarnings("all")
		public Inner(@Other.TA final int someVal) {
			this.someVal = someVal;
		}

		@Other.TA
		@java.lang.SuppressWarnings("all")
		public int getSomeVal() {
			return this.someVal;
		}
	}

	public static class Other {

		@Retention(RetentionPolicy.RUNTIME)
		public @interface TA {
		}
	}
}
