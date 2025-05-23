import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
public @Jacksonized @Builder @com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder.class) class JacksonizedBuilderSingular {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") class JacksonizedBuilderSingularBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> any$key;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<Object> any$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> values;
    private @java.lang.SuppressWarnings("all") @lombok.Generated com.google.common.collect.ImmutableMap.Builder<String, Object> guavaAny;
    private @java.lang.SuppressWarnings("all") @lombok.Generated com.google.common.collect.ImmutableList.Builder<String> guavaValues;
    @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingularBuilder() {
      super();
    }
    public @JsonAnySetter @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder any(final String anyKey, final Object anyValue) {
      if ((this.any$key == null))
          {
            this.any$key = new java.util.ArrayList<String>();
            this.any$value = new java.util.ArrayList<Object>();
          }
      this.any$key.add(anyKey);
      this.any$value.add(anyValue);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder any(final java.util.Map<? extends String, ? extends Object> any) {
      if ((any == null))
          {
            throw new java.lang.NullPointerException("any cannot be null");
          }
      if ((this.any$key == null))
          {
            this.any$key = new java.util.ArrayList<String>();
            this.any$value = new java.util.ArrayList<Object>();
          }
      for (java.util.Map.Entry<? extends String, ? extends Object> $lombokEntry : any.entrySet()) 
        {
          this.any$key.add($lombokEntry.getKey());
          this.any$value.add($lombokEntry.getValue());
        }
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder clearAny() {
      if ((this.any$key != null))
          {
            this.any$key.clear();
            this.any$value.clear();
          }
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder value(final String value) {
      if ((this.values == null))
          this.values = new java.util.ArrayList<String>();
      this.values.add(value);
      return this;
    }
    public @JsonProperty("v_a_l_u_e_s") @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder values(final java.util.Collection<? extends String> values) {
      if ((values == null))
          {
            throw new java.lang.NullPointerException("values cannot be null");
          }
      if ((this.values == null))
          this.values = new java.util.ArrayList<String>();
      this.values.addAll(values);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder clearValues() {
      if ((this.values != null))
          this.values.clear();
      return this;
    }
    public @JsonAnySetter @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder guavaAny(final String key, final Object value) {
      if ((this.guavaAny == null))
          this.guavaAny = com.google.common.collect.ImmutableMap.builder();
      this.guavaAny.put(key, value);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder guavaAny(final java.util.Map<? extends String, ? extends Object> guavaAny) {
      if ((guavaAny == null))
          {
            throw new java.lang.NullPointerException("guavaAny cannot be null");
          }
      if ((this.guavaAny == null))
          this.guavaAny = com.google.common.collect.ImmutableMap.builder();
      this.guavaAny.putAll(guavaAny);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder clearGuavaAny() {
      this.guavaAny = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder guavaValue(final String guavaValue) {
      if ((this.guavaValues == null))
          this.guavaValues = com.google.common.collect.ImmutableList.builder();
      this.guavaValues.add(guavaValue);
      return this;
    }
    public @JsonProperty("guava_v_a_l_u_e_s") @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder guavaValues(final java.lang.Iterable<? extends String> guavaValues) {
      if ((guavaValues == null))
          {
            throw new java.lang.NullPointerException("guavaValues cannot be null");
          }
      if ((this.guavaValues == null))
          this.guavaValues = com.google.common.collect.ImmutableList.builder();
      this.guavaValues.addAll(guavaValues);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder clearGuavaValues() {
      this.guavaValues = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular build() {
      java.util.Map<String, Object> any;
      switch (((this.any$key == null) ? 0 : this.any$key.size())) {
      case 0 :
          any = java.util.Collections.emptyMap();
          break;
      case 1 :
          any = java.util.Collections.singletonMap(this.any$key.get(0), this.any$value.get(0));
          break;
      default :
          any = new java.util.LinkedHashMap<String, Object>(((this.any$key.size() < 0x40000000) ? ((1 + this.any$key.size()) + ((this.any$key.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
          for (int $i = 0;; ($i < this.any$key.size()); $i ++) 
            any.put(this.any$key.get($i), this.any$value.get($i));
          any = java.util.Collections.unmodifiableMap(any);
      }
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
      com.google.common.collect.ImmutableMap<String, Object> guavaAny = ((this.guavaAny == null) ? com.google.common.collect.ImmutableMap.<String, Object>of() : this.guavaAny.build());
      com.google.common.collect.ImmutableList<String> guavaValues = ((this.guavaValues == null) ? com.google.common.collect.ImmutableList.<String>of() : this.guavaValues.build());
      return new JacksonizedBuilderSingular(any, values, guavaAny, guavaValues);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((((((("JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder(any$key=" + this.any$key) + ", any$value=") + this.any$value) + ", values=") + this.values) + ", guavaAny=") + this.guavaAny) + ", guavaValues=") + this.guavaValues) + ")");
    }
  }
  private @JsonAnySetter @Singular("any") Map<String, Object> any;
  private @JsonProperty("v_a_l_u_e_s") @Singular List<String> values;
  private @JsonAnySetter @Singular("guavaAny") ImmutableMap<String, Object> guavaAny;
  private @JsonProperty("guava_v_a_l_u_e_s") @Singular ImmutableList<String> guavaValues;
  @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular(final Map<String, Object> any, final List<String> values, final ImmutableMap<String, Object> guavaAny, final ImmutableList<String> guavaValues) {
    super();
    this.any = any;
    this.values = values;
    this.guavaAny = guavaAny;
    this.guavaValues = guavaValues;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder builder() {
    return new JacksonizedBuilderSingular.JacksonizedBuilderSingularBuilder();
  }
}