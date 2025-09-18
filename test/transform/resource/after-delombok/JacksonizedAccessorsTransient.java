public class JacksonizedAccessorsTransient {
	@com.fasterxml.jackson.annotation.JsonIgnore
	private transient int intValue;
	@com.fasterxml.jackson.annotation.JsonIgnore
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int intValue() {
		return this.intValue;
	}
	/**
	 * @return {@code this}.
	 */
	@com.fasterxml.jackson.annotation.JsonIgnore
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public JacksonizedAccessorsTransient intValue(final int intValue) {
		this.intValue = intValue;
		return this;
	}
}