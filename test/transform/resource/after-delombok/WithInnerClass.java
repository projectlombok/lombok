public class WithInnerClass<Z> {

	class Inner<Y> {
		final String x;

		@java.lang.SuppressWarnings("all")
		public Inner(final String x) {
			this.x = x;
		}

		@java.lang.SuppressWarnings("all")
		public WithInnerClass<Z>.Inner<Y> withX(final String x) {
			return this.x == x ? this : new WithInnerClass<Z>.Inner<Y>(x);
		}
	}
}