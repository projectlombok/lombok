import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
class EqualsAndHashCodeWithNonNullByDefault {
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(@javax.annotation.Nullable final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeWithNonNullByDefault)) return false;
		final EqualsAndHashCodeWithNonNullByDefault other = (EqualsAndHashCodeWithNonNullByDefault) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(@javax.annotation.Nullable final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeWithNonNullByDefault;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int result = 1;
		return result;
	}
}
