@lombok.extern.jackson.Jacksonized
@lombok.experimental.Accessors(fluent = true)
@lombok.Getter
@lombok.Setter
public class JacksonizedAccessorsTransient {
	private transient int intValue;
	@com.fasterxml.jackson.annotation.JsonIgnore private transient long longValue;
	@com.fasterxml.jackson.annotation.JsonIgnore private double doubleValue;
}