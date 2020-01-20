import java.util.List;
import lombok.Singular;
import lombok.Singular.NullCollectionBehavior;
@lombok.Builder class BuilderSingularNullBehavior2 {
  public static @java.lang.SuppressWarnings("all") class BuilderSingularNullBehavior2Builder {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> locations;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> doohickeys;
    @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2Builder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder location(final String location) {
      if ((this.locations == null))
          this.locations = new java.util.ArrayList<String>();
      this.locations.add(location);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder locations(final java.util. @org.checkerframework.checker.nullness.qual.NonNull Collection<? extends String> locations) {
      java.util.Objects.requireNonNull(locations, "locations cannot be null");
      if ((this.locations == null))
          this.locations = new java.util.ArrayList<String>();
      this.locations.addAll(locations);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder clearLocations() {
      if ((this.locations != null))
          this.locations.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder doohickey(final String doohickey) {
      if ((this.doohickeys == null))
          this.doohickeys = new java.util.ArrayList<String>();
      this.doohickeys.add(doohickey);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder doohickeys(final java.util. @org.checkerframework.checker.nullness.qual.Nullable Collection<? extends String> doohickeys) {
      if ((doohickeys != null))
          {
            if ((this.doohickeys == null))
                this.doohickeys = new java.util.ArrayList<String>();
            this.doohickeys.addAll(doohickeys);
          }
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder clearDoohickeys() {
      if ((this.doohickeys != null))
          this.doohickeys.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2 build() {
      java.util.List<String> locations;
      switch (((this.locations == null) ? 0 : this.locations.size())) {
      case 0 :
          locations = java.util.Collections.emptyList();
          break;
      case 1 :
          locations = java.util.Collections.singletonList(this.locations.get(0));
          break;
      default :
          locations = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.locations));
      }
      java.util.List<String> doohickeys;
      switch (((this.doohickeys == null) ? 0 : this.doohickeys.size())) {
      case 0 :
          doohickeys = java.util.Collections.emptyList();
          break;
      case 1 :
          doohickeys = java.util.Collections.singletonList(this.doohickeys.get(0));
          break;
      default :
          doohickeys = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.doohickeys));
      }
      return new BuilderSingularNullBehavior2(locations, doohickeys);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.@org.checkerframework.checker.nullness.qual.NonNull String toString() {
      return (((("BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder(locations=" + this.locations) + ", doohickeys=") + this.doohickeys) + ")");
    }
  }
  private @Singular(nullBehavior = Singular.NullCollectionBehavior.JDK) List<String> locations;
  private @Singular(nullBehavior = lombok.Singular.NullCollectionBehavior.IGNORE) List<String> doohickeys;
  @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2(final List<String> locations, final List<String> doohickeys) {
    super();
    this.locations = locations;
    this.doohickeys = doohickeys;
  }
  public static @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder builder() {
    return new BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder();
  }
}
