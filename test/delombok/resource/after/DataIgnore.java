class DataIgnore {
	final int x;
	String $name;
	public DataIgnore(final int x) {
		this.x = x;
	}
	public int getX() {
		return x;
	}
	@java.lang.Override
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (o.getClass() != this.getClass()) return false;
		final DataIgnore other = (DataIgnore)o;
		if (this.x != other.x) return false;
		return true;
	}
	@java.lang.Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = result * PRIME + this.x;
		return result;
	}
	@java.lang.Override
	public java.lang.String toString() {
		return "DataIgnore(x=" + x + ")";
	}
}
