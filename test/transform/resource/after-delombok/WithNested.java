public interface WithNested<Z> {
	class IAmStaticReally {
		final String x;
		@java.lang.SuppressWarnings("all")
		public IAmStaticReally(final String x) {
			this.x = x;
		}
		@java.lang.SuppressWarnings("all")
		public WithNested.IAmStaticReally withX(final String x) {
			return this.x == x ? this : new WithNested.IAmStaticReally(x);
		}
	}
}
