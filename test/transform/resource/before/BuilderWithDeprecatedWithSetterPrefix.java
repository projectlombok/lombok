import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;

@Builder(setterPrefix = "with")
public class BuilderWithDeprecated {
	/** @deprecated since always */ String dep1;
	@Deprecated int dep2;
	@Singular @Deprecated java.util.List<String> strings;
	@Singular @Deprecated ImmutableList<Integer> numbers;
}
