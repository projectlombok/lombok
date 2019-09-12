import java.util.List;
import java.util.Collection;

import lombok.Singular;

@lombok.Builder(setterPrefix = "with")
class BuilderSingularListsWithSetterPrefix<T> {
	@Singular private List<T> children;
	@Singular private Collection<? extends Number> scarves;
	@SuppressWarnings("all") @Singular("rawList") private List rawList;
}
