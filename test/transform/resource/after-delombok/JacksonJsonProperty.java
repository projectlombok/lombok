import com.fasterxml.jackson.annotation.JsonProperty;
public class JacksonJsonProperty {
	@JsonProperty("kebab-case-prop")
	public String kebabCaseProp;
	@java.lang.SuppressWarnings("all")
	JacksonJsonProperty(final String kebabCaseProp) {
		this.kebabCaseProp = kebabCaseProp;
	}
	@java.lang.SuppressWarnings("all")
	public static class JacksonJsonPropertyBuilder {
		@java.lang.SuppressWarnings("all")
		private String kebabCaseProp;
		@java.lang.SuppressWarnings("all")
		JacksonJsonPropertyBuilder() {
		}
		@JsonProperty("kebab-case-prop")
		@java.lang.SuppressWarnings("all")
		public JacksonJsonPropertyBuilder kebabCaseProp(final String kebabCaseProp) {
			this.kebabCaseProp = kebabCaseProp;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public JacksonJsonProperty build() {
			return new JacksonJsonProperty(kebabCaseProp);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "JacksonJsonProperty.JacksonJsonPropertyBuilder(kebabCaseProp=" + this.kebabCaseProp + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static JacksonJsonPropertyBuilder builder() {
		return new JacksonJsonPropertyBuilder();
	}
	@JsonProperty("kebab-case-prop")
	@java.lang.SuppressWarnings("all")
	public void setKebabCaseProp(final String kebabCaseProp) {
		this.kebabCaseProp = kebabCaseProp;
	}
}