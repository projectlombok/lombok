class EqualsAndHashCodeExplicitInclude {
	int x;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeExplicitInclude)) return false;
		final EqualsAndHashCodeExplicitInclude other = (EqualsAndHashCodeExplicitInclude) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeExplicitInclude;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int result = 1;
		return result;
	}
}