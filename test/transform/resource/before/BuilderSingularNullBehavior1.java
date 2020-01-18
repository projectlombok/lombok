import java.util.List;
import java.util.Collection;

import lombok.Singular;
import lombok.Singular.NullCollectionBehavior;

@lombok.Builder
class BuilderSingularNullBehavior1 {
	@Singular private List<String> names;
	@Singular(nullBehavior = Singular.NullCollectionBehavior.JDK) private List<String> locations;
	@Singular(nullBehavior = NullCollectionBehavior.GUAVA) private List<String> whatevers;
	@Singular(nullBehavior = lombok.Singular.NullCollectionBehavior.IGNORE) private List<String> doohickeys;
}
