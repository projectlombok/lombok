class DataConfiguration {
	final int x;
	@java.lang.SuppressWarnings("all")
	public DataConfiguration(final int x) {
		this.x = x;
	}
	@java.lang.SuppressWarnings("all")
	private DataConfiguration() {
		this.x = 0;
	}
	@java.lang.SuppressWarnings("all")
	public int getX() {
		return this.x;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof DataConfiguration)) return false;
		final DataConfiguration other = (DataConfiguration) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.x != other.x) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof DataConfiguration;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.x;
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "DataConfiguration(x=" + this.x + ")";
	}
}
