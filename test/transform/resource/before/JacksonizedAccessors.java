import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Accessors(fluent = true)
@Getter
@Setter
public class JacksonizedAccessors {
	private int intValue;
}
