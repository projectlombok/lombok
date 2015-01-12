import java.util.List;
import java.util.Collection;

import lombok.Singular;

@lombok.Builder
class BuilderSingletonLists<T> {
	@Singular private List<T> children;
	@Singular private Collection<? extends Number> scarves;
	@Singular("rawList") private List rawList;
}
