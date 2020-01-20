//CONF: lombok.addNullAnnotations = checkerframework
import java.util.List;

import lombok.Singular;
import lombok.Singular.NullCollectionBehavior;

@lombok.Builder
class BuilderSingularNullBehavior2 {
	@Singular(nullBehavior = Singular.NullCollectionBehavior.JDK) private List<String> locations;
	@Singular(nullBehavior = lombok.Singular.NullCollectionBehavior.IGNORE) private List<String> doohickeys;
}
