class EqualsAndHashCodeConfigKeys1Parent {
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeConfigKeys1Parent)) return false;
		final EqualsAndHashCodeConfigKeys1Parent other = (EqualsAndHashCodeConfigKeys1Parent) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeConfigKeys1Parent;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		int result = 1;
		return result;
	}
}
class EqualsAndHashCodeConfigKeys1 extends EqualsAndHashCodeConfigKeys1Parent {
	int x;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeConfigKeys1)) return false;
		final EqualsAndHashCodeConfigKeys1 other = (EqualsAndHashCodeConfigKeys1) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.x != other.x) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeConfigKeys1;
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
