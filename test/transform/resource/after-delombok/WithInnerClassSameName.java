interface A {

	class Inner<X> {
	}
}

class WithInnerClassSameName<Z> {

	class Inner<Y> implements A {
		final String x;

		@java.lang.SuppressWarnings("all")
		public Inner(final String x) {
			this.x = x;
		}

		@java.lang.SuppressWarnings("all")
		public WithInnerClassSameName<Z>.Inner<Y> withX(final String x) {
			return this.x == x ? this : new WithInnerClassSameName<Z>.Inner<Y>(x);
		}
	}
}