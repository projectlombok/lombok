// version 21:
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
class BuilderSingularSequencedCollections<T, K, V> {
	private SequencedCollection<T> sequencedCollection;
	private SequencedMap<K, V> sequencedMap;
	private SequencedSet<T> sequencedSet;
	@java.lang.SuppressWarnings("all")
	BuilderSingularSequencedCollections(final SequencedCollection<T> sequencedCollection, final SequencedMap<K, V> sequencedMap, final SequencedSet<T> sequencedSet) {
		this.sequencedCollection = sequencedCollection;
		this.sequencedMap = sequencedMap;
		this.sequencedSet = sequencedSet;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingularSequencedCollectionsBuilder<T, K, V> {
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<T> sequencedCollection;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<K> sequencedMap$key;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<V> sequencedMap$value;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<T> sequencedSet;
		@java.lang.SuppressWarnings("all")
		BuilderSingularSequencedCollectionsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> col(final T col) {
			if (this.sequencedCollection == null) this.sequencedCollection = new java.util.ArrayList<T>();
			this.sequencedCollection.add(col);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> sequencedCollection(final java.util.Collection<? extends T> sequencedCollection) {
			if (sequencedCollection == null) {
				throw new java.lang.NullPointerException("sequencedCollection cannot be null");
			}
			if (this.sequencedCollection == null) this.sequencedCollection = new java.util.ArrayList<T>();
			this.sequencedCollection.addAll(sequencedCollection);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> clearSequencedCollection() {
			if (this.sequencedCollection != null) this.sequencedCollection.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> map(final K mapKey, final V mapValue) {
			if (this.sequencedMap$key == null) {
				this.sequencedMap$key = new java.util.ArrayList<K>();
				this.sequencedMap$value = new java.util.ArrayList<V>();
			}
			this.sequencedMap$key.add(mapKey);
			this.sequencedMap$value.add(mapValue);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> sequencedMap(final java.util.Map<? extends K, ? extends V> sequencedMap) {
			if (sequencedMap == null) {
				throw new java.lang.NullPointerException("sequencedMap cannot be null");
			}
			if (this.sequencedMap$key == null) {
				this.sequencedMap$key = new java.util.ArrayList<K>();
				this.sequencedMap$value = new java.util.ArrayList<V>();
			}
			for (final java.util.Map.Entry<? extends K, ? extends V> $lombokEntry : sequencedMap.entrySet()) {
				this.sequencedMap$key.add($lombokEntry.getKey());
				this.sequencedMap$value.add($lombokEntry.getValue());
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> clearSequencedMap() {
			if (this.sequencedMap$key != null) {
				this.sequencedMap$key.clear();
				this.sequencedMap$value.clear();
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> set(final T set) {
			if (this.sequencedSet == null) this.sequencedSet = new java.util.ArrayList<T>();
			this.sequencedSet.add(set);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> sequencedSet(final java.util.Collection<? extends T> sequencedSet) {
			if (sequencedSet == null) {
				throw new java.lang.NullPointerException("sequencedSet cannot be null");
			}
			if (this.sequencedSet == null) this.sequencedSet = new java.util.ArrayList<T>();
			this.sequencedSet.addAll(sequencedSet);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> clearSequencedSet() {
			if (this.sequencedSet != null) this.sequencedSet.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularSequencedCollections<T, K, V> build() {
			java.util.SequencedCollection<T> sequencedCollection;
			switch (this.sequencedCollection == null ? 0 : this.sequencedCollection.size()) {
			case 0:
				sequencedCollection = java.util.Collections.emptyList();
				break;
			case 1:
				sequencedCollection = java.util.Collections.singletonList(this.sequencedCollection.get(0));
				break;
			default:
				sequencedCollection = java.util.Collections.unmodifiableList(new java.util.ArrayList<T>(this.sequencedCollection));
			}
			java.util.SequencedMap<K, V> sequencedMap = new java.util.LinkedHashMap<K, V>();
			if (this.sequencedMap$key != null) for (int $i = 0; $i < (this.sequencedMap$key == null ? 0 : this.sequencedMap$key.size()); $i++) sequencedMap.put(this.sequencedMap$key.get($i), (V) this.sequencedMap$value.get($i));
			sequencedMap = java.util.Collections.unmodifiableSequencedMap(sequencedMap);
			java.util.SequencedSet<T> sequencedSet = new java.util.LinkedHashSet<T>();
			if (this.sequencedSet != null) sequencedSet.addAll(this.sequencedSet);
			sequencedSet = java.util.Collections.unmodifiableSequencedSet(sequencedSet);
			return new BuilderSingularSequencedCollections<T, K, V>(sequencedCollection, sequencedMap, sequencedSet);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder(sequencedCollection=" + this.sequencedCollection + ", sequencedMap$key=" + this.sequencedMap$key + ", sequencedMap$value=" + this.sequencedMap$value + ", sequencedSet=" + this.sequencedSet + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <T, K, V> BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> builder() {
		return new BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V>();
	}
}
