@lombok.Data @lombok.experimental.Accessors(fluent = true) @lombok.extern.jackson.Jacksonized class JacksonizedAccessorsJackson3 {
  private @com.fasterxml.jackson.annotation.JsonProperty("name") String name;
  private @com.fasterxml.jackson.annotation.JsonProperty("age") int age;
  private transient @com.fasterxml.jackson.annotation.JsonIgnore String password;
  public @com.fasterxml.jackson.annotation.JsonProperty("name") @java.lang.SuppressWarnings("all") @lombok.Generated String name() {
    return this.name;
  }
  public @com.fasterxml.jackson.annotation.JsonProperty("age") @java.lang.SuppressWarnings("all") @lombok.Generated int age() {
    return this.age;
  }
  public @com.fasterxml.jackson.annotation.JsonIgnore @java.lang.SuppressWarnings("all") @lombok.Generated String password() {
    return this.password;
  }
  /**
   * @return {@code this}.
   */
  public @com.fasterxml.jackson.annotation.JsonProperty("name") @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessorsJackson3 name(final String name) {
    this.name = name;
    return this;
  }
  /**
   * @return {@code this}.
   */
  public @com.fasterxml.jackson.annotation.JsonProperty("age") @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessorsJackson3 age(final int age) {
    this.age = age;
    return this;
  }
  /**
   * @return {@code this}.
   */
  public @com.fasterxml.jackson.annotation.JsonIgnore @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessorsJackson3 password(final String password) {
    this.password = password;
    return this;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof JacksonizedAccessorsJackson3)))
        return false;
    final JacksonizedAccessorsJackson3 other = (JacksonizedAccessorsJackson3) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.age() != other.age()))
        return false;
    final java.lang.Object this$name = this.name();
    final java.lang.Object other$name = other.name();
    if (((this$name == null) ? (other$name != null) : (! this$name.equals(other$name))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof JacksonizedAccessorsJackson3);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.age());
    final java.lang.Object $name = this.name();
    result = ((result * PRIME) + (($name == null) ? 43 : $name.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((((("JacksonizedAccessorsJackson3(name=" + this.name()) + ", age=") + this.age()) + ", password=") + this.password()) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessorsJackson3() {
    super();
  }
}
