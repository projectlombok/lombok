class EqualsAndHashCodeEmpty {
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeEmpty)) return false;
		final EqualsAndHashCodeEmpty other = (EqualsAndHashCodeEmpty) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeEmpty;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int result = 1;
		return result;
	}
}
class EqualsAndHashCodeEmptyWithSuper extends EqualsAndHashCodeEmpty {
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeEmptyWithSuper)) return false;
		final EqualsAndHashCodeEmptyWithSuper other = (EqualsAndHashCodeEmptyWithSuper) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (!super.equals(o)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeEmptyWithSuper;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int result = super.hashCode();
		return result;
	}
}
