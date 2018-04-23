public interface EqualsAndHashCodeWithGenericsOnInnersInInterfaces<A> {
	class Inner<B> {
		int x;
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof EqualsAndHashCodeWithGenericsOnInnersInInterfaces.Inner)) return false;
			final EqualsAndHashCodeWithGenericsOnInnersInInterfaces.Inner<?> other = (EqualsAndHashCodeWithGenericsOnInnersInInterfaces.Inner<?>) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			if (this.x != other.x) return false;
			return true;
		}
		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof EqualsAndHashCodeWithGenericsOnInnersInInterfaces.Inner;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.x;
			return result;
		}
	}
}
