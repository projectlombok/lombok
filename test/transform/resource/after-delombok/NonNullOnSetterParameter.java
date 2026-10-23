class NonNullOnSetterParameter {
	String[] elements;
	String value;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setElements(@lombok.NonNull final String[] elements) {
		this.elements = elements;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setValue(@lombok.NonNull final String value) {
		if (value == null) {
			throw new java.lang.NullPointerException("value is marked non-null but is null");
		}
		this.value = value;
	}
}
