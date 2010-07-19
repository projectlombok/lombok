class Data1 {
	final int x;
	String name;
	@java.beans.ConstructorProperties({"x"})
	@java.lang.SuppressWarnings("all")
	public Data1(final int x) {
		this.x = x;
	}
	@java.lang.SuppressWarnings("all")
	public int getX() {
		return this.x;
	}
	@java.lang.SuppressWarnings("all")
	public String getName() {
		return this.name;
	}
	@java.lang.SuppressWarnings("all")
	public void setName(final String name) {
		this.name = name;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
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
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = result * PRIME + this.x;
		result = result * PRIME + (this.name == null ? 0 : this.name.hashCode());
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "Data1(x=" + this.x + ", name=" + this.name + ")";
	}
}
class Data2 {
	final int x;
	String name;
	@java.beans.ConstructorProperties({"x"})
	@java.lang.SuppressWarnings("all")
	public Data2(final int x) {
		this.x = x;
	}
	@java.lang.SuppressWarnings("all")
	public int getX() {
		return this.x;
	}
	@java.lang.SuppressWarnings("all")
	public String getName() {
		return this.name;
	}
	@java.lang.SuppressWarnings("all")
	public void setName(final String name) {
		this.name = name;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
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
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = result * PRIME + this.x;
		result = result * PRIME + (this.name == null ? 0 : this.name.hashCode());
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "Data2(x=" + this.x + ", name=" + this.name + ")";
	}
}
