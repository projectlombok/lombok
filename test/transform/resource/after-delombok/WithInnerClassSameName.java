interface A {

	class Inner<X> {
	}
}

class WithInnerClassSameName<Z> {

	class Inner<Y> implements A {
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
		public WithInnerClassSameName<Z>.Inner<Y> withX(final String x) {
			return this.x == x ? this : new WithInnerClassSameName<Z>.Inner<Y>(x);
		}
	}
}