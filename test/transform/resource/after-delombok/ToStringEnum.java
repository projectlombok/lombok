enum ToStringEnum1 {
	CONSTANT;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "ToStringEnum1." + this.name();
	}
}
enum ToStringEnum2 {
	CONSTANT;
	int x;
	String name;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "ToStringEnum2." + this.name() + "(x=" + this.x + ", name=" + this.name + ")";
	}
}
class ToStringEnum3 {
	enum MemberEnum {
		CONSTANT;
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "ToStringEnum3.MemberEnum." + this.name();
		}
	}
}
