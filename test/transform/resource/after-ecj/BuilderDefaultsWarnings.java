import lombok.Builder;
public @Builder class BuilderDefaultsWarnings {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class BuilderDefaultsWarningsBuilder {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") long x;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int z;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsWarningsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsWarningsBuilder x(final long x) {
      this.x = x;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsWarningsBuilder z(final int z) {
      this.z = z;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsWarnings build() {
      return new BuilderDefaultsWarnings(x, z);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((("BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder(x=" + this.x) + ", z=") + this.z) + ")");
    }
  }
  long x = System.currentTimeMillis();
  final int y = 5;
  @Builder.Default int z;
  @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsWarnings(final long x, final int z) {
    super();
    this.x = x;
    this.z = z;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsWarningsBuilder builder() {
    return new BuilderDefaultsWarningsBuilder();
  }
}
