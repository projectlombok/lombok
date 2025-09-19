import lombok.Builder;
import lombok.ToString;

@ToString(exclude = "secret")
@Builder
public class BuilderWithToStringExcludeOld {
	private String a;
	private String secret;
}