import java.util.List;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
public @lombok.extern.jackson.Jacksonized @lombok.Builder @JsonIgnoreProperties @com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedOnRecord.JacksonizedOnRecordBuilder.class) record JacksonizedOnRecord(String string, List values) {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated @JsonIgnoreProperties @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") class JacksonizedOnRecordBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String string;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> values;
    @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedOnRecordBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @JsonProperty("test") @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedOnRecord.JacksonizedOnRecordBuilder string(final @Nullable String string) {
      this.string = string;
      return this;
    }
    public @JsonAnySetter @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedOnRecord.JacksonizedOnRecordBuilder value(final String value) {
      if ((this.values == null))
          this.values = new java.util.ArrayList<String>();
      this.values.add(value);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedOnRecord.JacksonizedOnRecordBuilder values(final java.util.Collection<? extends String> values) {
      if ((values == null))
          {
            throw new java.lang.NullPointerException("values cannot be null");
          }
      if ((this.values == null))
          this.values = new java.util.ArrayList<String>();
      this.values.addAll(values);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedOnRecord.JacksonizedOnRecordBuilder clearValues() {
      if ((this.values != null))
          this.values.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedOnRecord build() {
      java.util.List<String> values;
      switch (((this.values == null) ? 0 : this.values.size())) {
      case 0 :
          values = java.util.Collections.emptyList();
          break;
      case 1 :
          values = java.util.Collections.singletonList(this.values.get(0));
          break;
      default :
          values = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.values));
      }
      return new JacksonizedOnRecord(this.string, values);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("JacksonizedOnRecord.JacksonizedOnRecordBuilder(string=" + this.string) + ", values=") + this.values) + ")");
    }
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedOnRecord.JacksonizedOnRecordBuilder builder() {
    return new JacksonizedOnRecord.JacksonizedOnRecordBuilder();
  }
}
