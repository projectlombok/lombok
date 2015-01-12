import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import lombok.Singular;

@lombok.Builder
class BuilderSingletonGuavaListsSets<T> {
	@Singular private ImmutableList<T> cards;
	@Singular private ImmutableCollection<? extends Number> frogs;
	@Singular("rawSet") private ImmutableSet rawSet;
	@Singular private ImmutableSortedSet<String> passes;
}
