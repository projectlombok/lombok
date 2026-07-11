import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
public sealed interface JacksonizedBuilderValueOnSealedInner permits JacksonizedBuilderValueOnSealedInner.MyInnerDTO {
  final @Value @Builder @Jacksonized @com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder.class) @tools.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder.class) class MyInnerDTO implements JacksonizedBuilderValueOnSealedInner {
    public static @java.lang.SuppressWarnings("all") @lombok.Generated @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") @tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") class MyInnerDTOBuilder {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String field;
      @java.lang.SuppressWarnings("all") @lombok.Generated MyInnerDTOBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder field(final String field) {
        this.field = field;
        return this;
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderValueOnSealedInner.MyInnerDTO build() {
        return new JacksonizedBuilderValueOnSealedInner.MyInnerDTO(this.field);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder(field=" + this.field) + ")");
      }
    }
    private final String field;
    @java.lang.SuppressWarnings("all") @lombok.Generated MyInnerDTO(final String field) {
      super();
      this.field = field;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder builder() {
      return new JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated String getField() {
      return this.field;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof JacksonizedBuilderValueOnSealedInner.MyInnerDTO)))
          return false;
      final JacksonizedBuilderValueOnSealedInner.MyInnerDTO other = (JacksonizedBuilderValueOnSealedInner.MyInnerDTO) o;
      final java.lang.Object this$field = this.getField();
      final java.lang.Object other$field = other.getField();
      if (((this$field == null) ? (other$field != null) : (! this$field.equals(other$field))))
          return false;
      return true;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final java.lang.Object $field = this.getField();
      result = ((result * PRIME) + (($field == null) ? 43 : $field.hashCode()));
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("JacksonizedBuilderValueOnSealedInner.MyInnerDTO(field=" + this.getField()) + ")");
    }
  }
}
