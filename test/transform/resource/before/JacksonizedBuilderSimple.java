//version 8: Jackson deps are at least Java7+.
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.extern.jackson.Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.Builder(access = lombok.AccessLevel.PROTECTED)
class JacksonizedBuilderSimple<T> {
	private final int noshow = 0;
	private final int yes;
	private List<T> also;
	private int $butNotMe;
}
