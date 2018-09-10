//VERSION 8:
import java.util.Set;
import java.util.Map;

import lombok.NonNull;
import lombok.Singular;

@lombok.Builder
class BuilderSingularAnnotatedTypes {
	@Singular private Set<@NonNull String> foos;
	@Singular private Map<@NonNull String, @NonNull Integer> bars;
}
