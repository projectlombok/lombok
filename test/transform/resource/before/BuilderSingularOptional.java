import java.util.Optional;

import lombok.Singular;

@lombok.Builder
class BuilderSingularOptional<T> {
	@Singular private Optional<T> value;
	@Singular private Optional<String> name;
}
