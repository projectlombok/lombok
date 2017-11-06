class ValueParent {
}
final class ValueCallSuper extends ValueParent {
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public ValueCallSuper() {
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof ValueCallSuper)) return false;
		final ValueCallSuper other = (ValueCallSuper) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (!super.equals(o)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof ValueCallSuper;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public int hashCode() {
		int result = super.hashCode();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public java.lang.String toString() {
		return "ValueCallSuper()";
	}
}
