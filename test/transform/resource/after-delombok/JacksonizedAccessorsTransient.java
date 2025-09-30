public class JacksonizedAccessorsTransient {
	@com.fasterxml.jackson.annotation.JsonIgnore
	private transient int intValue;
	@com.fasterxml.jackson.annotation.JsonIgnore
	private transient long longValue;
	@com.fasterxml.jackson.annotation.JsonIgnore
	private double doubleValue;

	@com.fasterxml.jackson.annotation.JsonIgnore
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int intValue() {
		return this.intValue;
	}
	
	@com.fasterxml.jackson.annotation.JsonIgnore
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public long longValue() {
		return this.longValue;
	}
	
	@com.fasterxml.jackson.annotation.JsonIgnore
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public double doubleValue() {
		return this.doubleValue;
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
	
	/**
	 * @return {@code this}.
	 */
	@com.fasterxml.jackson.annotation.JsonIgnore
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public JacksonizedAccessorsTransient longValue(final long longValue) {
		this.longValue = longValue;
		return this;
	}
	
	/**
	 * @return {@code this}.
	 */
	@com.fasterxml.jackson.annotation.JsonIgnore
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public JacksonizedAccessorsTransient doubleValue(final double doubleValue) {
		this.doubleValue = doubleValue;
		return this;
	}
}