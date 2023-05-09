import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
public @Jacksonized @Builder @com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder.class) class JacksonBuilderSimpleWithJsonUnwrapped {
  public static class Embedded {
    private int value;
    public Embedded() {
      super();
    }
  }
  public static @java.lang.SuppressWarnings("all") @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") class JacksonBuilderSimpleWithJsonUnwrappedBuilder {
    private @java.lang.SuppressWarnings("all") Embedded embedded;
    @java.lang.SuppressWarnings("all") JacksonBuilderSimpleWithJsonUnwrappedBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @JsonUnwrapped @java.lang.SuppressWarnings("all") JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder embedded(final @JsonUnwrapped Embedded embedded) {
      this.embedded = embedded;
      return this;
    }
    public @java.lang.SuppressWarnings("all") JacksonBuilderSimpleWithJsonUnwrapped build() {
      return new JacksonBuilderSimpleWithJsonUnwrapped(this.embedded);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder(embedded=" + this.embedded) + ")");
    }
  }
  private @JsonUnwrapped Embedded embedded;
  @java.lang.SuppressWarnings("all") JacksonBuilderSimpleWithJsonUnwrapped(final @JsonUnwrapped Embedded embedded) {
    super();
    this.embedded = embedded;
  }
  public static @java.lang.SuppressWarnings("all") JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder builder() {
    return new JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder();
  }
}
