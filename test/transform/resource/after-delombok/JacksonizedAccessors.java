public class JacksonizedAccessors {
	@com.fasterxml.jackson.annotation.JsonProperty("intValue")
	private int intValue;
	@java.lang.SuppressWarnings("all")
	@com.fasterxml.jackson.annotation.JsonProperty("intValue")
	public int intValue() {
		return this.intValue;
	}
	/**
	 * @return {@code this}.
	 */
	@java.lang.SuppressWarnings("all")
	@com.fasterxml.jackson.annotation.JsonProperty("intValue")
	public JacksonizedAccessors intValue(final int intValue) {
		this.intValue = intValue;
		return this;
	}
}