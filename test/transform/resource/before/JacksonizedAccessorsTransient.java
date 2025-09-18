@lombok.extern.jackson.Jacksonized
@lombok.experimental.Accessors(fluent = true)
@lombok.Getter
@lombok.Setter
public class JacksonizedAccessorsTransient {
	private transient int intValue;
}