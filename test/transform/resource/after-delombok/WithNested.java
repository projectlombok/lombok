public interface WithNested<Z> {
	class IAmStaticReally {
		final String x;
		@java.lang.SuppressWarnings("all")
		public IAmStaticReally(final String x) {
			this.x = x;
		}
		/**
		 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
		 */
		@java.lang.SuppressWarnings("all")
		public WithNested.IAmStaticReally withX(final String x) {
			return this.x == x ? this : new WithNested.IAmStaticReally(x);
		}
	}
}
