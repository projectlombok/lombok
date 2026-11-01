import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
public sealed interface JacksonizedBuilderOnSealedInner permits JacksonizedBuilderOnSealedInner.MyInnerDTO {
  final @Builder @Jacksonized @com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder.class) @tools.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder.class) class MyInnerDTO implements JacksonizedBuilderOnSealedInner {
    public static @java.lang.SuppressWarnings("all") @lombok.Generated @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") @tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") class MyInnerDTOBuilder {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String field;
      @java.lang.SuppressWarnings("all") @lombok.Generated MyInnerDTOBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder field(final String field) {
        this.field = field;
        return this;
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderOnSealedInner.MyInnerDTO build() {
        return new JacksonizedBuilderOnSealedInner.MyInnerDTO(this.field);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder(field=" + this.field) + ")");
      }
    }
    String field;
    @java.lang.SuppressWarnings("all") @lombok.Generated MyInnerDTO(final String field) {
      super();
      this.field = field;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder builder() {
      return new JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder();
    }
  }
}
