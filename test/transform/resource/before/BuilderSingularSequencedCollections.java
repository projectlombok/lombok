// version 21:

import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;

import lombok.Builder;
import lombok.Singular;

@Builder
class BuilderSingularSequencedCollections<T, K, V> {
	@Singular("col") private SequencedCollection<T> sequencedCollection;
	@Singular("map") private SequencedMap<K, V> sequencedMap;
	@Singular("set") private SequencedSet<T> sequencedSet;
}
