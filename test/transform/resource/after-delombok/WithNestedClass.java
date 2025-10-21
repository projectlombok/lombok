public class WithNestedClass<Z> {

	static class Inner<Z> {
		final String x;

		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public Inner(final String x) {
			this.x = x;
		}

		/**
		 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public WithNestedClass.Inner<Z> withX(final String x) {
			return this.x == x ? this : new WithNestedClass.Inner<Z>(x);
		}
	}
}