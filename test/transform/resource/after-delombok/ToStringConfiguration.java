class ToStringConfiguration {
	int x;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public java.lang.String toString() {
		return "ToStringConfiguration(" + this.x + ")";
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public int getX() {
		return this.x;
	}
}
class ToStringConfiguration2 {
	int x;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public java.lang.String toString() {
		return "ToStringConfiguration2(x=" + this.x + ")";
	}
}
class ToStringConfiguration3 {
	int x;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public java.lang.String toString() {
		return "ToStringConfiguration3(" + this.getX() + ")";
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public int getX() {
		return this.x;
	}
}
class ToStringConfiguration4 {
	java.lang.String[] array;
	java.util.List<java.lang.String> strings;
	java.util.Set<java.lang.Integer> set;
	java.util.Map<java.lang.String, java.lang.Integer> map;
	int x;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public java.lang.String toString() {
		return "ToStringConfiguration4(" + "array.length=" + this.array != null ? array.length : null + ", strings.size=" + this.strings != null ? strings.size() : null + ", set.size=" + this.set != null ? set.size() : null + ", map.size=" + this.map != null ? map.size() : null + ", x=" + this.x + ")";
	}
}