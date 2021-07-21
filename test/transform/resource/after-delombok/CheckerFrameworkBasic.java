// skip-idempotent
//version 8:
class CheckerFrameworkBasic {
	private final int x;
	private final int y;
	private int z;
	@org.checkerframework.dataflow.qual.Pure
	@java.lang.SuppressWarnings("all")
	public int getX() {
		return this.x;
	}
	@org.checkerframework.dataflow.qual.Pure
	@java.lang.SuppressWarnings("all")
	public int getY() {
		return this.y;
	}
	@org.checkerframework.dataflow.qual.SideEffectFree
	@java.lang.SuppressWarnings("all")
	public int getZ() {
		return this.z;
	}
	/**
	 * @return {@code this}.
	 */
	@java.lang.SuppressWarnings("all")
	public @org.checkerframework.common.returnsreceiver.qual.This CheckerFrameworkBasic setZ(final int z) {
		this.z = z;
		return this;
	}
	@org.checkerframework.dataflow.qual.SideEffectFree
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof CheckerFrameworkBasic)) return false;
		final CheckerFrameworkBasic other = (CheckerFrameworkBasic) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.getX() != other.getX()) return false;
		if (this.getY() != other.getY()) return false;
		if (this.getZ() != other.getZ()) return false;
		return true;
	}
	@org.checkerframework.dataflow.qual.Pure
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof CheckerFrameworkBasic;
	}
	@org.checkerframework.dataflow.qual.SideEffectFree
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getX();
		result = result * PRIME + this.getY();
		result = result * PRIME + this.getZ();
		return result;
	}
	@org.checkerframework.dataflow.qual.SideEffectFree
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "CheckerFrameworkBasic(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ")";
	}
	@java.lang.SuppressWarnings("all")
	public CheckerFrameworkBasic(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	/**
	 * @return {@code this}.
	 */
	@org.checkerframework.dataflow.qual.SideEffectFree
	@java.lang.SuppressWarnings("all")
	public CheckerFrameworkBasic withX(final int x) {
		return this.x == x ? this : new CheckerFrameworkBasic(x, this.y, this.z);
	}
}
