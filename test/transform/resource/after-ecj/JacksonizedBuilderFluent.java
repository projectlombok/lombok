import com.fasterxml.jackson.annotation.JsonProperty;
public @lombok.extern.jackson.Jacksonized @lombok.experimental.Accessors(fluent = true) @lombok.Builder @com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder.class) @tools.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder.class) class JacksonizedBuilderFluent {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") @tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") class JacksonizedBuilderFluentBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String fieldName1;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String fieldName2;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String fieldName3;
    @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderFluentBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder fieldName1(final String fieldName1) {
      this.fieldName1 = fieldName1;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @JsonProperty("FieldName2") @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder fieldName2(final String fieldName2) {
      this.fieldName2 = fieldName2;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder fieldName3(final String fieldName3) {
      this.fieldName3 = fieldName3;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderFluent build() {
      return new JacksonizedBuilderFluent(this.fieldName1, this.fieldName2, this.fieldName3);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder(fieldName1=" + this.fieldName1) + ", fieldName2=") + this.fieldName2) + ", fieldName3=") + this.fieldName3) + ")");
    }
  }
  private @com.fasterxml.jackson.annotation.JsonProperty("fieldName1") String fieldName1;
  private @JsonProperty("FieldName2") String fieldName2;
  private @com.fasterxml.jackson.annotation.JsonProperty("fieldName3") String fieldName3;
  @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderFluent(final String fieldName1, final String fieldName2, final String fieldName3) {
    super();
    this.fieldName1 = fieldName1;
    this.fieldName2 = fieldName2;
    this.fieldName3 = fieldName3;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder builder() {
    return new JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder();
  }
}
