import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Accessors(fluent = true)
@Getter
public class JacksonizedAccessors {
	private int intValue;
}
