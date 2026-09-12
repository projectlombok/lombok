// version 21:
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import lombok.Builder;
import lombok.Singular;
@lombok.Builder class BuilderSingularSequencedCollections<T, K, V> {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderSingularSequencedCollectionsBuilder<T, K, V> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<T> sequencedCollection;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<K> sequencedMap$key;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<V> sequencedMap$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<T> sequencedSet;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollectionsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> col(final T col) {
      if ((this.sequencedCollection == null))
          this.sequencedCollection = new java.util.ArrayList<T>();
      this.sequencedCollection.add(col);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> sequencedCollection(final java.util.Collection<? extends T> sequencedCollection) {
      if ((sequencedCollection == null))
          {
            throw new java.lang.NullPointerException("sequencedCollection cannot be null");
          }
      if ((this.sequencedCollection == null))
          this.sequencedCollection = new java.util.ArrayList<T>();
      this.sequencedCollection.addAll(sequencedCollection);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> clearSequencedCollection() {
      if ((this.sequencedCollection != null))
          this.sequencedCollection.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> map(final K mapKey, final V mapValue) {
      if ((this.sequencedMap$key == null))
          {
            this.sequencedMap$key = new java.util.ArrayList<K>();
            this.sequencedMap$value = new java.util.ArrayList<V>();
          }
      this.sequencedMap$key.add(mapKey);
      this.sequencedMap$value.add(mapValue);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> sequencedMap(final java.util.Map<? extends K, ? extends V> sequencedMap) {
      if ((sequencedMap == null))
          {
            throw new java.lang.NullPointerException("sequencedMap cannot be null");
          }
      if ((this.sequencedMap$key == null))
          {
            this.sequencedMap$key = new java.util.ArrayList<K>();
            this.sequencedMap$value = new java.util.ArrayList<V>();
          }
      for (java.util.Map.Entry<? extends K, ? extends V> $lombokEntry : sequencedMap.entrySet())
        {
          this.sequencedMap$key.add($lombokEntry.getKey());
          this.sequencedMap$value.add($lombokEntry.getValue());
        }
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> clearSequencedMap() {
      if ((this.sequencedMap$key != null))
          {
            this.sequencedMap$key.clear();
            this.sequencedMap$value.clear();
          }
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> set(final T set) {
      if ((this.sequencedSet == null))
          this.sequencedSet = new java.util.ArrayList<T>();
      this.sequencedSet.add(set);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> sequencedSet(final java.util.Collection<? extends T> sequencedSet) {
      if ((sequencedSet == null))
          {
            throw new java.lang.NullPointerException("sequencedSet cannot be null");
          }
      if ((this.sequencedSet == null))
          this.sequencedSet = new java.util.ArrayList<T>();
      this.sequencedSet.addAll(sequencedSet);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> clearSequencedSet() {
      if ((this.sequencedSet != null))
          this.sequencedSet.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections<T, K, V> build() {
      java.util.SequencedCollection<T> sequencedCollection;
      switch (((this.sequencedCollection == null) ? 0 : this.sequencedCollection.size())) {
      case 0 :
          sequencedCollection = java.util.Collections.emptyList();
          break;
      case 1 :
          sequencedCollection = java.util.Collections.singletonList(this.sequencedCollection.get(0));
          break;
      default :
          sequencedCollection = java.util.Collections.unmodifiableList(new java.util.ArrayList<T>(this.sequencedCollection));
      }
      java.util.SequencedMap<K, V> sequencedMap = new java.util.LinkedHashMap<K, V>();
      if ((this.sequencedMap$key != null))
          for (int $i = 0;; ($i < ((this.sequencedMap$key == null) ? 0 : this.sequencedMap$key.size())); $i ++)
            sequencedMap.put(this.sequencedMap$key.get($i), this.sequencedMap$value.get($i));
      sequencedMap = java.util.Collections.unmodifiableSequencedMap(sequencedMap);
      java.util.SequencedSet<T> sequencedSet = new java.util.LinkedHashSet<T>();
      if ((this.sequencedSet != null))
          sequencedSet.addAll(this.sequencedSet);
      sequencedSet = java.util.Collections.unmodifiableSequencedSet(sequencedSet);
      return new BuilderSingularSequencedCollections<T, K, V>(sequencedCollection, sequencedMap, sequencedSet);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((((("BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder(sequencedCollection=" + this.sequencedCollection) + ", sequencedMap$key=") + this.sequencedMap$key) + ", sequencedMap$value=") + this.sequencedMap$value) + ", sequencedSet=") + this.sequencedSet) + ")");
    }
  }
  private @Singular("col") SequencedCollection<T> sequencedCollection;
  private @Singular("map") SequencedMap<K, V> sequencedMap;
  private @Singular("set") SequencedSet<T> sequencedSet;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularSequencedCollections(final SequencedCollection<T> sequencedCollection, final SequencedMap<K, V> sequencedMap, final SequencedSet<T> sequencedSet) {
    super();
    this.sequencedCollection = sequencedCollection;
    this.sequencedMap = sequencedMap;
    this.sequencedSet = sequencedSet;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated <T, K, V>BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V> builder() {
    return new BuilderSingularSequencedCollections.BuilderSingularSequencedCollectionsBuilder<T, K, V>();
  }
}