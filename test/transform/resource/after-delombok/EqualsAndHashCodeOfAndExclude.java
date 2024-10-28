final class EqualsAndHashCodeOf {
	int x;
	int y;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeOf)) return false;
		final EqualsAndHashCodeOf other = (EqualsAndHashCodeOf) o;
		if (this.x != other.x) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.x;
		return result;
	}
}
final class EqualsAndHashCodeExclude {
	int x;
	int y;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeExclude)) return false;
		final EqualsAndHashCodeExclude other = (EqualsAndHashCodeExclude) o;
		if (this.x != other.x) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.x;
		return result;
	}
}
