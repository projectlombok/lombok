//version 8: Jackson deps are at least Java7+.
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Singular;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder.class)
public class JacksonBuilderSimpleWithJsonUnwrapped {

	public static class Embedded {
		private int value;
	}

	@JsonUnwrapped
	private Embedded embedded;

	@java.lang.SuppressWarnings("all")
	JacksonBuilderSimpleWithJsonUnwrapped(@JsonUnwrapped final Embedded embedded) {
		this.embedded = embedded;
	}


	@java.lang.SuppressWarnings("all")
	@com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
	public static class JacksonBuilderSimpleWithJsonUnwrappedBuilder {
		@java.lang.SuppressWarnings("all")
		private Embedded embedded;

		@java.lang.SuppressWarnings("all")
		JacksonBuilderSimpleWithJsonUnwrappedBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		@JsonUnwrapped
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder embedded(@JsonUnwrapped final Embedded embedded) {
			this.embedded = embedded;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSimpleWithJsonUnwrapped build() {
			return new JacksonBuilderSimpleWithJsonUnwrapped(this.embedded);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder(embedded=" + this.embedded + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder builder() {
		return new JacksonBuilderSimpleWithJsonUnwrapped.JacksonBuilderSimpleWithJsonUnwrappedBuilder();
	}
}
