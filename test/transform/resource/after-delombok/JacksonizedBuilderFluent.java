import com.fasterxml.jackson.annotation.JsonProperty;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder.class)
@tools.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder.class)
public class JacksonizedBuilderFluent {
	@com.fasterxml.jackson.annotation.JsonProperty("fieldName1")
	private String fieldName1;
	@JsonProperty("FieldName2")
	private String fieldName2;
	@com.fasterxml.jackson.annotation.JsonProperty("fieldName3")
	private String fieldName3;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	JacksonizedBuilderFluent(final String fieldName1, final String fieldName2, final String fieldName3) {
		this.fieldName1 = fieldName1;
		this.fieldName2 = fieldName2;
		this.fieldName3 = fieldName3;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	@com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
	@tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
	public static class JacksonizedBuilderFluentBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String fieldName1;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String fieldName2;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String fieldName3;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		JacksonizedBuilderFluentBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder fieldName1(final String fieldName1) {
			this.fieldName1 = fieldName1;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@JsonProperty("FieldName2")
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder fieldName2(final String fieldName2) {
			this.fieldName2 = fieldName2;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder fieldName3(final String fieldName3) {
			this.fieldName3 = fieldName3;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedBuilderFluent build() {
			return new JacksonizedBuilderFluent(this.fieldName1, this.fieldName2, this.fieldName3);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder(fieldName1=" + this.fieldName1 + ", fieldName2=" + this.fieldName2 + ", fieldName3=" + this.fieldName3 + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder builder() {
		return new JacksonizedBuilderFluent.JacksonizedBuilderFluentBuilder();
	}
}
