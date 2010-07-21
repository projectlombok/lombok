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
		if (this.getX() != other.getX()) return false;
		if (this.getName() == null ? other.getName() != null : !this.getName().equals(other.getName())) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = result * PRIME + this.getX();
		result = result * PRIME + (this.getName() == null ? 0 : this.getName().hashCode());
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "Data1(x=" + this.getX() + ", name=" + this.getName() + ")";
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
		if (this.getX() != other.getX()) return false;
		if (this.getName() == null ? other.getName() != null : !this.getName().equals(other.getName())) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = result * PRIME + this.getX();
		result = result * PRIME + (this.getName() == null ? 0 : this.getName().hashCode());
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "Data2(x=" + this.getX() + ", name=" + this.getName() + ")";
	}
}
