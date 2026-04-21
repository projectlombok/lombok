import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
public @Data class JacksonJsonProperty2 {
  @JsonProperty("a") String propertyOne;
  @JsonProperty("b") int propertyTwo;
  Map<String, Object> additional = new HashMap<>();
  public @JsonProperty("a") @java.lang.SuppressWarnings("all") @lombok.Generated String getPropertyOne() {
    return this.propertyOne;
  }
  public @JsonProperty("b") @java.lang.SuppressWarnings("all") @lombok.Generated int getPropertyTwo() {
    return this.propertyTwo;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated Map<String, Object> getAdditional() {
    return this.additional;
  }
  public @JsonProperty("a") @java.lang.SuppressWarnings("all") @lombok.Generated void setPropertyOne(final String propertyOne) {
    this.propertyOne = propertyOne;
  }
  public @JsonProperty("b") @java.lang.SuppressWarnings("all") @lombok.Generated void setPropertyTwo(final int propertyTwo) {
    this.propertyTwo = propertyTwo;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setAdditional(final Map<String, Object> additional) {
    this.additional = additional;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof JacksonJsonProperty2)))
        return false;
    final JacksonJsonProperty2 other = (JacksonJsonProperty2) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getPropertyTwo() != other.getPropertyTwo()))
        return false;
    final java.lang.Object this$propertyOne = this.getPropertyOne();
    final java.lang.Object other$propertyOne = other.getPropertyOne();
    if (((this$propertyOne == null) ? (other$propertyOne != null) : (! this$propertyOne.equals(other$propertyOne))))
        return false;
    final java.lang.Object this$additional = this.getAdditional();
    final java.lang.Object other$additional = other.getAdditional();
    if (((this$additional == null) ? (other$additional != null) : (! this$additional.equals(other$additional))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof JacksonJsonProperty2);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getPropertyTwo());
    final java.lang.Object $propertyOne = this.getPropertyOne();
    result = ((result * PRIME) + (($propertyOne == null) ? 43 : $propertyOne.hashCode()));
    final java.lang.Object $additional = this.getAdditional();
    result = ((result * PRIME) + (($additional == null) ? 43 : $additional.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((((("JacksonJsonProperty2(propertyOne=" + this.getPropertyOne()) + ", propertyTwo=") + this.getPropertyTwo()) + ", additional=") + this.getAdditional()) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonJsonProperty2() {
    super();
  }
}