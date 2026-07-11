public sealed interface JacksonizedBuilderValueOnSealedInner permits JacksonizedBuilderValueOnSealedInner.MyInnerDTO {
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder.class)
	@tools.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder.class)
	final class MyInnerDTO implements JacksonizedBuilderValueOnSealedInner {
		private final String field;
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
			public JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder field(final String field) {
				this.field = field;
				return this;
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public JacksonizedBuilderValueOnSealedInner.MyInnerDTO build() {
				return new JacksonizedBuilderValueOnSealedInner.MyInnerDTO(this.field);
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder(field=" + this.field + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder builder() {
			return new JacksonizedBuilderValueOnSealedInner.MyInnerDTO.MyInnerDTOBuilder();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public String getField() {
			return this.field;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof JacksonizedBuilderValueOnSealedInner.MyInnerDTO)) return false;
			final JacksonizedBuilderValueOnSealedInner.MyInnerDTO other = (JacksonizedBuilderValueOnSealedInner.MyInnerDTO) o;
			final java.lang.Object this$field = this.getField();
			final java.lang.Object other$field = other.getField();
			if (this$field == null ? other$field != null : !this$field.equals(other$field)) return false;
			return true;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final java.lang.Object $field = this.getField();
			result = result * PRIME + ($field == null ? 43 : $field.hashCode());
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "JacksonizedBuilderValueOnSealedInner.MyInnerDTO(field=" + this.getField() + ")";
		}
	}
}
