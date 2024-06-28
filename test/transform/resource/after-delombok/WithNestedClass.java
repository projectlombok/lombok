public class WithNestedClass<Z> {

	static class Inner<Z> {
		final String x;

		@java.lang.SuppressWarnings("all")
		public Inner(final String x) {
			this.x = x;
		}

		@java.lang.SuppressWarnings("all")
		public WithNestedClass.Inner<Z> withX(final String x) {
			return this.x == x ? this : new WithNestedClass.Inner<Z>(x);
		}
	}
}