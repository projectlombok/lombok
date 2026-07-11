public sealed interface JacksonizedBuilderOnSealedInner permits JacksonizedBuilderOnSealedInner.MyInnerDTO {
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder.class)
	@tools.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder.class)
	final class MyInnerDTO implements JacksonizedBuilderOnSealedInner {
		String field;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		MyInnerDTO(final String field) {
			this.field = field;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		@com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
		@tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
		public static class MyInnerDTOBuilder {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private String field;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			MyInnerDTOBuilder() {
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder field(final String field) {
				this.field = field;
				return this;
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public JacksonizedBuilderOnSealedInner.MyInnerDTO build() {
				return new JacksonizedBuilderOnSealedInner.MyInnerDTO(this.field);
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder(field=" + this.field + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder builder() {
			return new JacksonizedBuilderOnSealedInner.MyInnerDTO.MyInnerDTOBuilder();
		}
	}
}
