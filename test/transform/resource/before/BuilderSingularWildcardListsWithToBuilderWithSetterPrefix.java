import java.util.List;
import java.util.Collection;

import lombok.Singular;

@lombok.Builder(toBuilder = true, setterPrefix = "with")
class BuilderSingularWildcardListsWithToBuilderWithSetterPrefix {
	@Singular private List<?> objects;
	@Singular private Collection<? extends Number> numbers;
}
