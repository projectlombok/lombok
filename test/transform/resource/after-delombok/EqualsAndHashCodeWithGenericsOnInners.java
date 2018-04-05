//version 7:
public class EqualsAndHashCodeWithGenericsOnInners<A> {
	class Inner<B> {
		int x;
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof EqualsAndHashCodeWithGenericsOnInners.Inner)) return false;
			final EqualsAndHashCodeWithGenericsOnInners<?>.Inner<?> other = (EqualsAndHashCodeWithGenericsOnInners<?>.Inner<?>) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			if (this.x != other.x) return false;
			return true;
		}
		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof EqualsAndHashCodeWithGenericsOnInners.Inner;
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

