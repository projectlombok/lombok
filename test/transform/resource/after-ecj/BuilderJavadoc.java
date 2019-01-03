import java.util.List;
@lombok.Builder class BuilderJavadoc<T> {
  public static @java.lang.SuppressWarnings("all") class BuilderJavadocBuilder<T> {
    private @java.lang.SuppressWarnings("all") int yes;
    private @java.lang.SuppressWarnings("all") List<T> also;
    @java.lang.SuppressWarnings("all") BuilderJavadocBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadocBuilder<T> yes(final int yes) {
      this.yes = yes;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadocBuilder<T> also(final List<T> also) {
      this.also = also;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadoc<T> build() {
      return new BuilderJavadoc<T>(yes, also);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((("BuilderJavadoc.BuilderJavadocBuilder(yes=" + this.yes) + ", also=") + this.also) + ")");
    }
  }
  private final int noshow = 0;
  private final int yes;
  private List<T> also;
  private int $butNotMe;
  @java.lang.SuppressWarnings("all") BuilderJavadoc(final int yes, final List<T> also) {
    super();
    this.yes = yes;
    this.also = also;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderJavadocBuilder<T> builder() {
    return new BuilderJavadocBuilder<T>();
  }
}
