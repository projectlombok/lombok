//CONF: lombok.copyJacksonAnnotationsToAccessors = true
//version 8: Jackson deps are at least Java7+.
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JacksonJsonProperty3 {
	@JsonProperty("a")
	String propertyOne;
	@JsonProperty("b")
	int propertyTwo;
	Map<String, Object> additional = new HashMap<>();

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public JacksonJsonProperty3() {
	}

	@JsonProperty("a")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getPropertyOne() {
		return this.propertyOne;
	}

	@JsonProperty("b")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getPropertyTwo() {
		return this.propertyTwo;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public Map<String, Object> getAdditional() {
		return this.additional;
	}

	@JsonProperty("a")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setPropertyOne(final String propertyOne) {
		this.propertyOne = propertyOne;
	}

	@JsonProperty("b")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setPropertyTwo(final int propertyTwo) {
		this.propertyTwo = propertyTwo;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setAdditional(final Map<String, Object> additional) {
		this.additional = additional;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof JacksonJsonProperty3)) return false;
		final JacksonJsonProperty3 other = (JacksonJsonProperty3) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.getPropertyTwo() != other.getPropertyTwo()) return false;
		final java.lang.Object this$propertyOne = this.getPropertyOne();
		final java.lang.Object other$propertyOne = other.getPropertyOne();
		if (this$propertyOne == null ? other$propertyOne != null : !this$propertyOne.equals(other$propertyOne)) return false;
		final java.lang.Object this$additional = this.getAdditional();
		final java.lang.Object other$additional = other.getAdditional();
		if (this$additional == null ? other$additional != null : !this$additional.equals(other$additional)) return false;
		return true;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof JacksonJsonProperty3;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getPropertyTwo();
		final java.lang.Object $propertyOne = this.getPropertyOne();
		result = result * PRIME + ($propertyOne == null ? 43 : $propertyOne.hashCode());
		final java.lang.Object $additional = this.getAdditional();
		result = result * PRIME + ($additional == null ? 43 : $additional.hashCode());
		return result;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "JacksonJsonProperty3(propertyOne=" + this.getPropertyOne() + ", propertyTwo=" + this.getPropertyTwo() + ", additional=" + this.getAdditional() + ")";
	}
}