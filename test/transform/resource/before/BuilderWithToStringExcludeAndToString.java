import lombok.Builder;
import lombok.ToString;
import lombok.ToString.Exclude;

@Builder
@ToString
public class BuilderWithToStringExcludeAndToString {
	private String a;
	@ToString.Exclude
	private String secret;
}