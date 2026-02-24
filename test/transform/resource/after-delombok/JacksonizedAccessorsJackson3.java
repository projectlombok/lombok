//version 8: Jackson deps are at least Java7+.
//CONF: lombok.jacksonized.jacksonVersion += 3
class JacksonizedAccessorsJackson3 {
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	private String name;
	@com.fasterxml.jackson.annotation.JsonProperty("age")
	private int age;
	@com.fasterxml.jackson.annotation.JsonIgnore
	private transient String password;
	
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public JacksonizedAccessorsJackson3() {
	}
	
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String name() {
		return this.name;
	}
	
	@com.fasterxml.jackson.annotation.JsonProperty("age")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int age() {
		return this.age;
	}
	
	@com.fasterxml.jackson.annotation.JsonIgnore
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String password() {
		return this.password;
	}
	
	/**
	 * @return {@code this}.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public JacksonizedAccessorsJackson3 name(final String name) {
		this.name = name;
		return this;
	}
	
	/**
	 * @return {@code this}.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("age")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public JacksonizedAccessorsJackson3 age(final int age) {
		this.age = age;
		return this;
	}
	
	/**
	 * @return {@code this}.
	 */
	@com.fasterxml.jackson.annotation.JsonIgnore
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public JacksonizedAccessorsJackson3 password(final String password) {
		this.password = password;
		return this;
	}
	
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof JacksonizedAccessorsJackson3)) return false;
		final JacksonizedAccessorsJackson3 other = (JacksonizedAccessorsJackson3) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.age() != other.age()) return false;
		final java.lang.Object this$name = this.name();
		final java.lang.Object other$name = other.name();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		return true;
	}
	
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof JacksonizedAccessorsJackson3;
	}
	
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.age();
		final java.lang.Object $name = this.name();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		return result;
	}
	
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "JacksonizedAccessorsJackson3(name=" + this.name() + ", age=" + this.age() + ", password=" + this.password() + ")";
	}
}
