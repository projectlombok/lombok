import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@lombok.extern.jackson.Jacksonized @JsonIgnoreProperties(ignoreUnknown = true) @lombok.Builder(access = lombok.AccessLevel.PROTECTED) @com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderSimple.JacksonizedBuilderSimpleBuilder.class) class JacksonizedBuilderSimple<T> {
  protected static @java.lang.SuppressWarnings("all") @lombok.Generated @JsonIgnoreProperties(ignoreUnknown = true) @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") class JacksonizedBuilderSimpleBuilder<T> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int yes;
    private @java.lang.SuppressWarnings("all") @lombok.Generated List<T> also;
    @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSimpleBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSimple.JacksonizedBuilderSimpleBuilder<T> yes(final int yes) {
      this.yes = yes;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSimple.JacksonizedBuilderSimpleBuilder<T> also(final List<T> also) {
      this.also = also;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSimple<T> build() {
      return new JacksonizedBuilderSimple<T>(this.yes, this.also);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("JacksonizedBuilderSimple.JacksonizedBuilderSimpleBuilder(yes=" + this.yes) + ", also=") + this.also) + ")");
    }
  }
  private final int noshow = 0;
  private final int yes;
  private List<T> also;
  private int $butNotMe;
  @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSimple(final int yes, final List<T> also) {
    super();
    this.yes = yes;
    this.also = also;
  }
  protected static @java.lang.SuppressWarnings("all") @lombok.Generated <T>JacksonizedBuilderSimple.JacksonizedBuilderSimpleBuilder<T> builder() {
    return new JacksonizedBuilderSimple.JacksonizedBuilderSimpleBuilder<T>();
  }
}
