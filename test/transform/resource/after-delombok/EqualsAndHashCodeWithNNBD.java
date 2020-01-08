import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class EqualsAndHashCodeWithNNBD {

	@org.eclipse.jdt.annotation.NonNullByDefault
	static class Inner {
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.@javax.annotation.Nullable @org.eclipse.jdt.annotation.Nullable Object o) {
			if (o == this) return true;
			if (!(o instanceof EqualsAndHashCodeWithNNBD.Inner)) return false;
			final EqualsAndHashCodeWithNNBD.Inner other = (EqualsAndHashCodeWithNNBD.Inner) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			return true;
		}

		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof EqualsAndHashCodeWithNNBD.Inner;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int result = 1;
			return result;
		}
	}
}