@interface Nullable {
}

class EqualsAndHashCode {
	int x;
	boolean[] y;
	Object[] z;
	String a;
	String b;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(@Nullable final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCode)) return false;
		final EqualsAndHashCode other = (EqualsAndHashCode)o;
		if (!other.canEqual((java.lang.Object)this)) return false;
		if (this.x != other.x) return false;
		if (!java.util.Arrays.equals(this.y, other.y)) return false;
		if (!java.util.Arrays.deepEquals(this.z, other.z)) return false;
		final java.lang.Object this$a = this.a;
		final java.lang.Object other$a = other.a;
		if (this$a == null ? other$a != null : !this$a.equals(other$a)) return false;
		final java.lang.Object this$b = this.b;
		final java.lang.Object other$b = other.b;
		if (this$b == null ? other$b != null : !this$b.equals(other$b)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(@Nullable final java.lang.Object other) {
		return other instanceof EqualsAndHashCode;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.x;
		result = result * PRIME + java.util.Arrays.hashCode(this.y);
		result = result * PRIME + java.util.Arrays.deepHashCode(this.z);
		final java.lang.Object $a = this.a;
		result = result * PRIME + ($a == null ? 0 : $a.hashCode());
		final java.lang.Object $b = this.b;
		result = result * PRIME + ($b == null ? 0 : $b.hashCode());
		return result;
	}
}