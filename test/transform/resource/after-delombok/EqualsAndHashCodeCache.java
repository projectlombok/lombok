class EqualsAndHashCode {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private transient int $hashCodeCache;
	int x;
	boolean[] y;
	Object[] z;
	String a;
	String b;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCode)) return false;
		final EqualsAndHashCode other = (EqualsAndHashCode) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
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
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCode;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		if (this.$hashCodeCache != 0) return this.$hashCodeCache;
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.x;
		result = result * PRIME + java.util.Arrays.hashCode(this.y);
		result = result * PRIME + java.util.Arrays.deepHashCode(this.z);
		final java.lang.Object $a = this.a;
		result = result * PRIME + ($a == null ? 43 : $a.hashCode());
		final java.lang.Object $b = this.b;
		result = result * PRIME + ($b == null ? 43 : $b.hashCode());
		if (result == 0) result = java.lang.Integer.MIN_VALUE;
		this.$hashCodeCache = result;
		return result;
	}
}
final class EqualsAndHashCode2 {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private transient int $hashCodeCache;
	int x;
	long y;
	float f;
	double d;
	boolean b;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCode2)) return false;
		final EqualsAndHashCode2 other = (EqualsAndHashCode2) o;
		if (this.x != other.x) return false;
		if (this.y != other.y) return false;
		if (java.lang.Float.compare(this.f, other.f) != 0) return false;
		if (java.lang.Double.compare(this.d, other.d) != 0) return false;
		if (this.b != other.b) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		if (this.$hashCodeCache != 0) return this.$hashCodeCache;
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.x;
		final long $y = this.y;
		result = result * PRIME + (int) ($y >>> 32 ^ $y);
		result = result * PRIME + java.lang.Float.floatToIntBits(this.f);
		final long $d = java.lang.Double.doubleToLongBits(this.d);
		result = result * PRIME + (int) ($d >>> 32 ^ $d);
		result = result * PRIME + (this.b ? 79 : 97);
		if (result == 0) result = java.lang.Integer.MIN_VALUE;
		this.$hashCodeCache = result;
		return result;
	}
}
final class EqualsAndHashCode3 extends EqualsAndHashCode {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private transient int $hashCodeCache;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCode3)) return false;
		final EqualsAndHashCode3 other = (EqualsAndHashCode3) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCode3;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		if (this.$hashCodeCache != 0) return this.$hashCodeCache;
		int result = 1;
		if (result == 0) result = java.lang.Integer.MIN_VALUE;
		this.$hashCodeCache = result;
		return result;
	}
}
class EqualsAndHashCode4 extends EqualsAndHashCode {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private transient int $hashCodeCache;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCode4)) return false;
		final EqualsAndHashCode4 other = (EqualsAndHashCode4) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (!super.equals(o)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCode4;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		if (this.$hashCodeCache != 0) return this.$hashCodeCache;
		int result = super.hashCode();
		if (result == 0) result = java.lang.Integer.MIN_VALUE;
		this.$hashCodeCache = result;
		return result;
	}
}
final class EqualsAndHashCode5 extends EqualsAndHashCode {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private transient int $hashCodeCache;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCode5)) return false;
		final EqualsAndHashCode5 other = (EqualsAndHashCode5) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (!super.equals(o)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCode5;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		if (this.$hashCodeCache != 0) return this.$hashCodeCache;
		int result = super.hashCode();
		if (result == 0) result = java.lang.Integer.MIN_VALUE;
		this.$hashCodeCache = result;
		return result;
	}
}
