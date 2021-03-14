//version 8: springframework dep is too new to run on j6
import java.util.List;
import lombok.Singular;
@lombok.Builder class BuilderSingularNullBehavior2 {
  public static @java.lang.SuppressWarnings("all") class BuilderSingularNullBehavior2Builder {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> locations;
    @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2Builder() {
      super();
    }
    public @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder location(final String location) {
      if ((this.locations == null))
          this.locations = new java.util.ArrayList<String>();
      this.locations.add(location);
      return this;
    }
    public @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder locations(final @org.springframework.lang.NonNull java.util.Collection<? extends String> locations) {
      java.util.Objects.requireNonNull(locations, "locations cannot be null");
      if ((this.locations == null))
          this.locations = new java.util.ArrayList<String>();
      this.locations.addAll(locations);
      return this;
    }
    public @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder clearLocations() {
      if ((this.locations != null))
          this.locations.clear();
      return this;
    }
    public @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2 build() {
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
      return new BuilderSingularNullBehavior2(locations);
    }
    public @java.lang.Override @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder(locations=" + this.locations) + ")");
    }
  }
  private @Singular List<String> locations;
  @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2(final List<String> locations) {
    super();
    this.locations = locations;
  }
  public static @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder builder() {
    return new BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder();
  }
}