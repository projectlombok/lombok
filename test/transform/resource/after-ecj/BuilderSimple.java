import java.util.List;
@lombok.Builder class BuilderSimple<T> {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class BuilderSimpleBuilder<T> {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int yes;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") List<T> also;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSimpleBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSimpleBuilder<T> yes(final int yes) {
      this.yes = yes;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSimpleBuilder<T> also(final List<T> also) {
      this.also = also;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSimple<T> build() {
      return new BuilderSimple<T>(yes, also);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((("BuilderSimple.BuilderSimpleBuilder(yes=" + this.yes) + ", also=") + this.also) + ")");
    }
  }
  private final int noshow = 0;
  private final int yes;
  private List<T> also;
  private int $butNotMe;
  @java.beans.ConstructorProperties({"yes", "also"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSimple(final int yes, final List<T> also) {
    super();
    this.yes = yes;
    this.also = also;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") <T>BuilderSimpleBuilder<T> builder() {
    return new BuilderSimpleBuilder<T>();
  }
}
