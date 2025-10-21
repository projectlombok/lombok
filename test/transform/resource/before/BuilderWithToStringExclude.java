import lombok.Builder;
import lombok.ToString;
import lombok.ToString.Exclude;

@Builder
public class BuilderWithToStringExclude {
	private String a;
	@ToString.Exclude
	private String secret;
}