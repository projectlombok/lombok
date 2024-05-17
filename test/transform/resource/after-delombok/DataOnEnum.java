public enum DataOnEnum {
	A("hello");
	private final String someField;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getSomeField() {
		return this.someField;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "DataOnEnum." + this.name() + "(someField=" + this.getSomeField() + ")";
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private DataOnEnum(final String someField) {
		this.someField = someField;
	}
}
