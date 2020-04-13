import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonBuilderSingular.JacksonBuilderSingularBuilder.class)
public class JacksonBuilderSingular {
	@JsonAnySetter
	public Map<String, Object> any;
	@JsonProperty("v_a_l_u_e_s")
	public Map<String, Object> values;
	@java.lang.SuppressWarnings("all")
	JacksonBuilderSingular(final Map<String, Object> any, final Map<String, Object> values) {
		this.any = any;
		this.values = values;
	}
	@java.lang.SuppressWarnings("all")
	@com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
	public static class JacksonBuilderSingularBuilder {
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> any$key;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<Object> any$value;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> values$key;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<Object> values$value;
		@java.lang.SuppressWarnings("all")
		JacksonBuilderSingularBuilder() {
		}
		@JsonAnySetter
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder any(final String anyKey, final Object anyValue) {
			if (this.any$key == null) {
				this.any$key = new java.util.ArrayList<String>();
				this.any$value = new java.util.ArrayList<Object>();
			}
			this.any$key.add(anyKey);
			this.any$value.add(anyValue);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder any(final java.util.Map<? extends String, ? extends Object> any) {
			if (any == null) {
				throw new java.lang.NullPointerException("any cannot be null");
			}
			if (this.any$key == null) {
				this.any$key = new java.util.ArrayList<String>();
				this.any$value = new java.util.ArrayList<Object>();
			}
			for (final java.util.Map.Entry<? extends String, ? extends Object> $lombokEntry : any.entrySet()) {
				this.any$key.add($lombokEntry.getKey());
				this.any$value.add($lombokEntry.getValue());
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder clearAny() {
			if (this.any$key != null) {
				this.any$key.clear();
				this.any$value.clear();
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder value(final String valueKey, final Object valueValue) {
			if (this.values$key == null) {
				this.values$key = new java.util.ArrayList<String>();
				this.values$value = new java.util.ArrayList<Object>();
			}
			this.values$key.add(valueKey);
			this.values$value.add(valueValue);
			return this;
		}
		@JsonProperty("v_a_l_u_e_s")
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder values(final java.util.Map<? extends String, ? extends Object> values) {
			if (values == null) {
				throw new java.lang.NullPointerException("values cannot be null");
			}
			if (this.values$key == null) {
				this.values$key = new java.util.ArrayList<String>();
				this.values$value = new java.util.ArrayList<Object>();
			}
			for (final java.util.Map.Entry<? extends String, ? extends Object> $lombokEntry : values.entrySet()) {
				this.values$key.add($lombokEntry.getKey());
				this.values$value.add($lombokEntry.getValue());
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder clearValues() {
			if (this.values$key != null) {
				this.values$key.clear();
				this.values$value.clear();
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular build() {
			java.util.Map<String, Object> any;
			switch (this.any$key == null ? 0 : this.any$key.size()) {
			case 0: 
				any = java.util.Collections.emptyMap();
				break;
			case 1: 
				any = java.util.Collections.singletonMap(this.any$key.get(0), this.any$value.get(0));
				break;
			default: 
				any = new java.util.LinkedHashMap<String, Object>(this.any$key.size() < 1073741824 ? 1 + this.any$key.size() + (this.any$key.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				for (int $i = 0; $i < this.any$key.size(); $i++) any.put(this.any$key.get($i), (Object) this.any$value.get($i));
				any = java.util.Collections.unmodifiableMap(any);
			}
			java.util.Map<String, Object> values;
			switch (this.values$key == null ? 0 : this.values$key.size()) {
			case 0: 
				values = java.util.Collections.emptyMap();
				break;
			case 1: 
				values = java.util.Collections.singletonMap(this.values$key.get(0), this.values$value.get(0));
				break;
			default: 
				values = new java.util.LinkedHashMap<String, Object>(this.values$key.size() < 1073741824 ? 1 + this.values$key.size() + (this.values$key.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				for (int $i = 0; $i < this.values$key.size(); $i++) values.put(this.values$key.get($i), (Object) this.values$value.get($i));
				values = java.util.Collections.unmodifiableMap(values);
			}
			return new JacksonBuilderSingular(any, values);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "JacksonBuilderSingular.JacksonBuilderSingularBuilder(any$key=" + this.any$key + ", any$value=" + this.any$value + ", values$key=" + this.values$key + ", values$value=" + this.values$value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static JacksonBuilderSingular.JacksonBuilderSingularBuilder builder() {
		return new JacksonBuilderSingular.JacksonBuilderSingularBuilder();
	}
}