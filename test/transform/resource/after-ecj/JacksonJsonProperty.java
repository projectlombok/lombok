import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Builder;
import lombok.Setter;
public @Builder class JacksonJsonProperty {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class JacksonJsonPropertyBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String kebabCaseProp;
    @java.lang.SuppressWarnings("all") @lombok.Generated JacksonJsonPropertyBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @JsonProperty("kebab-case-prop") @JsonSetter(nulls = Nulls.SKIP) @java.lang.SuppressWarnings("all") @lombok.Generated JacksonJsonProperty.JacksonJsonPropertyBuilder kebabCaseProp(final String kebabCaseProp) {
      this.kebabCaseProp = kebabCaseProp;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonJsonProperty build() {
      return new JacksonJsonProperty(this.kebabCaseProp);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("JacksonJsonProperty.JacksonJsonPropertyBuilder(kebabCaseProp=" + this.kebabCaseProp) + ")");
    }
  }
  public @JsonProperty("kebab-case-prop") @JsonSetter(nulls = Nulls.SKIP) @Setter String kebabCaseProp;
  @java.lang.SuppressWarnings("all") @lombok.Generated JacksonJsonProperty(final String kebabCaseProp) {
    super();
    this.kebabCaseProp = kebabCaseProp;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated JacksonJsonProperty.JacksonJsonPropertyBuilder builder() {
    return new JacksonJsonProperty.JacksonJsonPropertyBuilder();
  }
  public @JsonProperty("kebab-case-prop") @JsonSetter(nulls = Nulls.SKIP) @java.lang.SuppressWarnings("all") @lombok.Generated void setKebabCaseProp(final String kebabCaseProp) {
    this.kebabCaseProp = kebabCaseProp;
  }
}
