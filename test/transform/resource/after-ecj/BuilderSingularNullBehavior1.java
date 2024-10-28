import java.util.List;
import java.util.Collection;
import lombok.Singular;
@lombok.Builder class BuilderSingularNullBehavior1 {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderSingularNullBehavior1Builder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> names;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> locations;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1Builder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1.@org.checkerframework.checker.nullness.qual.NonNull BuilderSingularNullBehavior1Builder name(final String name) {
      if ((this.names == null))
          this.names = new java.util.ArrayList<String>();
      this.names.add(name);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1.@org.checkerframework.checker.nullness.qual.NonNull BuilderSingularNullBehavior1Builder names(final java.util. @org.checkerframework.checker.nullness.qual.NonNull Collection<? extends String> names) {
      assert (names != null): "names cannot be null";
      if ((this.names == null))
          this.names = new java.util.ArrayList<String>();
      this.names.addAll(names);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1.@org.checkerframework.checker.nullness.qual.NonNull BuilderSingularNullBehavior1Builder clearNames() {
      if ((this.names != null))
          this.names.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1.@org.checkerframework.checker.nullness.qual.NonNull BuilderSingularNullBehavior1Builder location(final String location) {
      if ((this.locations == null))
          this.locations = new java.util.ArrayList<String>();
      this.locations.add(location);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1.@org.checkerframework.checker.nullness.qual.NonNull BuilderSingularNullBehavior1Builder locations(final java.util. @org.checkerframework.checker.nullness.qual.Nullable Collection<? extends String> locations) {
      if ((locations != null))
          {
            if ((this.locations == null))
                this.locations = new java.util.ArrayList<String>();
            this.locations.addAll(locations);
          }
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1.@org.checkerframework.checker.nullness.qual.NonNull BuilderSingularNullBehavior1Builder clearLocations() {
      if ((this.locations != null))
          this.locations.clear();
      return this;
    }
    public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1 build() {
      java.util.List<String> names;
      switch (((this.names == null) ? 0 : this.names.size())) {
      case 0 :
          names = java.util.Collections.emptyList();
          break;
      case 1 :
          names = java.util.Collections.singletonList(this.names.get(0));
          break;
      default :
          names = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.names));
      }
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
      return new BuilderSingularNullBehavior1(names, locations);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.@org.checkerframework.checker.nullness.qual.NonNull String toString() {
      return (((("BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder(names=" + this.names) + ", locations=") + this.locations) + ")");
    }
  }
  private @Singular List<String> names;
  private @Singular(ignoreNullCollections = true) List<String> locations;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1(final List<String> names, final List<String> locations) {
    super();
    this.names = names;
    this.locations = locations;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularNullBehavior1.@org.checkerframework.checker.nullness.qual.NonNull BuilderSingularNullBehavior1Builder builder() {
    return new BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder();
  }
}
