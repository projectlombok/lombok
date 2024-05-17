//version 14:
import java.util.List;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedOnRecord.JacksonizedOnRecordBuilder.class)
public record JacksonizedOnRecord(@JsonProperty("test") @Nullable String string, @JsonAnySetter List<String> values) {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	@JsonIgnoreProperties
	@com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
	public static class JacksonizedOnRecordBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String string;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private java.util.ArrayList<String> values;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		JacksonizedOnRecordBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@JsonProperty("test")
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedOnRecord.JacksonizedOnRecordBuilder string(@Nullable final String string) {
			this.string = string;
			return this;
		}
		@JsonAnySetter
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedOnRecord.JacksonizedOnRecordBuilder value(final String value) {
			if (this.values == null) this.values = new java.util.ArrayList<String>();
			this.values.add(value);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedOnRecord.JacksonizedOnRecordBuilder values(final java.util.Collection<? extends String> values) {
			if (values == null) {
				throw new java.lang.NullPointerException("values cannot be null");
			}
			if (this.values == null) this.values = new java.util.ArrayList<String>();
			this.values.addAll(values);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedOnRecord.JacksonizedOnRecordBuilder clearValues() {
			if (this.values != null) this.values.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedOnRecord build() {
			java.util.List<String> values;
			switch (this.values == null ? 0 : this.values.size()) {
			case 0: 
				values = java.util.Collections.emptyList();
				break;
			case 1: 
				values = java.util.Collections.singletonList(this.values.get(0));
				break;
			default: 
				values = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.values));
			}
			return new JacksonizedOnRecord(this.string, values);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "JacksonizedOnRecord.JacksonizedOnRecordBuilder(string=" + this.string + ", values=" + this.values + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static JacksonizedOnRecord.JacksonizedOnRecordBuilder builder() {
		return new JacksonizedOnRecord.JacksonizedOnRecordBuilder();
	}
}
