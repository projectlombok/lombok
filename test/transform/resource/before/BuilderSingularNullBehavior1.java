//version 8:
//CONF: lombok.addNullAnnotations = checkerframework
//CONF: lombok.nonNull.exceptionType = assertion
import java.util.List;
import java.util.Collection;

import lombok.Singular;

@lombok.Builder
class BuilderSingularNullBehavior1 {
	@Singular private List<String> names;
	@Singular(ignoreNullCollections = true) private List<String> locations;
}
