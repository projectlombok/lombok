class Data1 {
	final int x;
	String name;
	public Data1(final int x) {
		this.x = x;
	}
	public int getX() {
		return x;
	}
	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	@java.lang.Override
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (o.getClass() != this.getClass()) return false;
		final Data1 other = (Data1)o;
		if (this.x != other.x) return false;
		if (this.name == null ? other.name != null : !this.name.equals(other.name)) return false;
		return true;
	}
	@java.lang.Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = result * PRIME + this.x;
		result = result * PRIME + (this.name == null ? 0 : this.name.hashCode());
		return result;
	}
	@java.lang.Override
	public java.lang.String toString() {
		return "Data1(x=" + x + ", name=" + name + ")";
	}
}
class Data2 {
	final int x;
	String name;
	public Data2(final int x) {
		this.x = x;
	}
	public int getX() {
		return x;
	}
	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	@java.lang.Override
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (o.getClass() != this.getClass()) return false;
		final Data2 other = (Data2)o;
		if (this.x != other.x) return false;
		if (this.name == null ? other.name != null : !this.name.equals(other.name)) return false;
		return true;
	}
	@java.lang.Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = result * PRIME + this.x;
		result = result * PRIME + (this.name == null ? 0 : this.name.hashCode());
		return result;
	}
	@java.lang.Override
	public java.lang.String toString() {
		return "Data2(x=" + x + ", name=" + name + ")";
	}
}
