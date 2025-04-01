public class JacksonizedAccessors {
	@com.fasterxml.jackson.annotation.JsonProperty("intValue")
	private int intValue;
	@com.fasterxml.jackson.annotation.JsonProperty("intValue")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int intValue() {
		return this.intValue;
	}
	/**
	 * @return {@code this}.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("intValue")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public JacksonizedAccessors intValue(final int intValue) {
		this.intValue = intValue;
		return this;
	}
}