//version 8: springframework dep is too new to run on j6
//CONF: lombok.addNullAnnotations = spring
//CONF: lombok.nonNull.exceptionType = JDK
import java.util.List;

import lombok.Singular;

@lombok.Builder
class BuilderSingularNullBehavior2 {
	@Singular private List<String> locations;
}
