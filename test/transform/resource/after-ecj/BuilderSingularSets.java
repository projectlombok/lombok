import java.util.Set;
import java.util.SortedSet;
import lombok.Singular;
@lombok.Builder class BuilderSingularSets<T> {
  public static @java.lang.SuppressWarnings("all") class BuilderSingularSetsBuilder<T> {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<T> dangerMice;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<Number> octopodes;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<java.lang.Object> rawSet;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> stringSet;
    @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> dangerMouse(final T dangerMouse) {
      if ((this.dangerMice == null))
          this.dangerMice = new java.util.ArrayList<T>();
      this.dangerMice.add(dangerMouse);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> dangerMice(final java.util.Collection<? extends T> dangerMice) {
      if ((this.dangerMice == null))
          this.dangerMice = new java.util.ArrayList<T>();
      this.dangerMice.addAll(dangerMice);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> clearDangerMice() {
      if ((this.dangerMice != null))
          this.dangerMice.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> octopus(final Number octopus) {
      if ((this.octopodes == null))
          this.octopodes = new java.util.ArrayList<Number>();
      this.octopodes.add(octopus);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> octopodes(final java.util.Collection<? extends Number> octopodes) {
      if ((this.octopodes == null))
          this.octopodes = new java.util.ArrayList<Number>();
      this.octopodes.addAll(octopodes);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> clearOctopodes() {
      if ((this.octopodes != null))
          this.octopodes.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> rawSet(final java.lang.Object rawSet) {
      if ((this.rawSet == null))
          this.rawSet = new java.util.ArrayList<java.lang.Object>();
      this.rawSet.add(rawSet);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> rawSet(final java.util.Collection<?> rawSet) {
      if ((this.rawSet == null))
          this.rawSet = new java.util.ArrayList<java.lang.Object>();
      this.rawSet.addAll(rawSet);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> clearRawSet() {
      if ((this.rawSet != null))
          this.rawSet.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> stringSet(final String stringSet) {
      if ((this.stringSet == null))
          this.stringSet = new java.util.ArrayList<String>();
      this.stringSet.add(stringSet);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> stringSet(final java.util.Collection<? extends String> stringSet) {
      if ((this.stringSet == null))
          this.stringSet = new java.util.ArrayList<String>();
      this.stringSet.addAll(stringSet);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSetsBuilder<T> clearStringSet() {
      if ((this.stringSet != null))
          this.stringSet.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularSets<T> build() {
      java.util.Set<T> dangerMice;
      switch (((this.dangerMice == null) ? 0 : this.dangerMice.size())) {
      case 0 :
          dangerMice = java.util.Collections.emptySet();
          break;
      case 1 :
          dangerMice = java.util.Collections.singleton(this.dangerMice.get(0));
          break;
      default :
          dangerMice = new java.util.LinkedHashSet<T>(((this.dangerMice.size() < 0x40000000) ? ((1 + this.dangerMice.size()) + ((this.dangerMice.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
          dangerMice.addAll(this.dangerMice);
          dangerMice = java.util.Collections.unmodifiableSet(dangerMice);
      }
      java.util.SortedSet<Number> octopodes = new java.util.TreeSet<Number>();
      if ((this.octopodes != null))
          octopodes.addAll(this.octopodes);
      octopodes = java.util.Collections.unmodifiableSortedSet(octopodes);
      java.util.Set<java.lang.Object> rawSet;
      switch (((this.rawSet == null) ? 0 : this.rawSet.size())) {
      case 0 :
          rawSet = java.util.Collections.emptySet();
          break;
      case 1 :
          rawSet = java.util.Collections.singleton(this.rawSet.get(0));
          break;
      default :
          rawSet = new java.util.LinkedHashSet<java.lang.Object>(((this.rawSet.size() < 0x40000000) ? ((1 + this.rawSet.size()) + ((this.rawSet.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
          rawSet.addAll(this.rawSet);
          rawSet = java.util.Collections.unmodifiableSet(rawSet);
      }
      java.util.Set<String> stringSet;
      switch (((this.stringSet == null) ? 0 : this.stringSet.size())) {
      case 0 :
          stringSet = java.util.Collections.emptySet();
          break;
      case 1 :
          stringSet = java.util.Collections.singleton(this.stringSet.get(0));
          break;
      default :
          stringSet = new java.util.LinkedHashSet<String>(((this.stringSet.size() < 0x40000000) ? ((1 + this.stringSet.size()) + ((this.stringSet.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
          stringSet.addAll(this.stringSet);
          stringSet = java.util.Collections.unmodifiableSet(stringSet);
      }
      return new BuilderSingularSets<T>(dangerMice, octopodes, rawSet, stringSet);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderSingularSets.BuilderSingularSetsBuilder(dangerMice=" + this.dangerMice) + ", octopodes=") + this.octopodes) + ", rawSet=") + this.rawSet) + ", stringSet=") + this.stringSet) + ")");
    }
  }
  private @Singular Set<T> dangerMice;
  private @Singular SortedSet<? extends Number> octopodes;
  private @SuppressWarnings("all") @Singular("rawSet") Set rawSet;
  private @Singular("stringSet") Set<String> stringSet;
  @java.lang.SuppressWarnings("all") BuilderSingularSets(final Set<T> dangerMice, final SortedSet<? extends Number> octopodes, final Set rawSet, final Set<String> stringSet) {
    super();
    this.dangerMice = dangerMice;
    this.octopodes = octopodes;
    this.rawSet = rawSet;
    this.stringSet = stringSet;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderSingularSetsBuilder<T> builder() {
    return new BuilderSingularSetsBuilder<T>();
  }
}