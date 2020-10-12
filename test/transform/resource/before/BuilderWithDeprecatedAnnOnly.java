import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;

@Builder
public class BuilderWithDeprecatedAnnOnly {
	@Deprecated int dep1;
	@Singular @Deprecated java.util.List<String> strings;
	@Singular @Deprecated ImmutableList<Integer> numbers;
}
