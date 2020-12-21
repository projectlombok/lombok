public class NullLibrary2 {
	String foo;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(@org.springframework.lang.Nullable final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof NullLibrary2)) return false;
		final NullLibrary2 other = (NullLibrary2) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		final java.lang.Object this$foo = this.foo;
		final java.lang.Object other$foo = other.foo;
		if (this$foo == null ? other$foo != null : !this$foo.equals(other$foo)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(@org.springframework.lang.Nullable final java.lang.Object other) {
		return other instanceof NullLibrary2;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final java.lang.Object $foo = this.foo;
		result = result * PRIME + ($foo == null ? 43 : $foo.hashCode());
		return result;
	}
	@org.springframework.lang.NonNull
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "NullLibrary2(foo=" + this.foo + ")";
	}
	@java.lang.SuppressWarnings("all")
	public NullLibrary2(final String foo) {
		this.foo = foo;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@org.springframework.lang.NonNull
	@java.lang.SuppressWarnings("all")
	public NullLibrary2 withFoo(final String foo) {
		return this.foo == foo ? this : new NullLibrary2(foo);
	}
}
