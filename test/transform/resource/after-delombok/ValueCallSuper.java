class ValueParent {
}
final class ValueCallSuper extends ValueParent {
	@java.lang.SuppressWarnings("all")
	public ValueCallSuper() {
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof ValueCallSuper)) return false;
		final ValueCallSuper other = (ValueCallSuper) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (!super.equals(o)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof ValueCallSuper;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		int result = super.hashCode();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "ValueCallSuper()";
	}
}
