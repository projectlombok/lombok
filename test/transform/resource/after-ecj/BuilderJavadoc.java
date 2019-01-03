import java.util.List;
@lombok.Builder class BuilderJavadoc<T> {
  public static class BuilderJavadocBuilder<T> {
    private @java.lang.SuppressWarnings("all") int yes;
    private @java.lang.SuppressWarnings("all") int getset;
    private @java.lang.SuppressWarnings("all") int predef;
    private @java.lang.SuppressWarnings("all") int predefWithJavadoc;
    private @java.lang.SuppressWarnings("all") List<T> also;
    public BuilderJavadocBuilder<T> predef(final int x) {
      this.predef = (x * 10);
      return this;
    }
    public BuilderJavadocBuilder<T> predefWithJavadoc(final int x) {
      this.predefWithJavadoc = (x * 100);
      return this;
    }
    @java.lang.SuppressWarnings("all") BuilderJavadocBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadocBuilder<T> yes(final int yes) {
      this.yes = yes;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadocBuilder<T> getset(final int getset) {
      this.getset = getset;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadocBuilder<T> also(final List<T> also) {
      this.also = also;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadoc<T> build() {
      return new BuilderJavadoc<T>(yes, getset, predef, predefWithJavadoc, also);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((((("BuilderJavadoc.BuilderJavadocBuilder(yes=" + this.yes) + ", getset=") + this.getset) + ", predef=") + this.predef) + ", predefWithJavadoc=") + this.predefWithJavadoc) + ", also=") + this.also) + ")");
    }
  }
  private final int noshow = 0;
  private final int yes;
  private @lombok.Getter @lombok.Setter int getset;
  private final int predef;
  private final int predefWithJavadoc;
  private List<T> also;
  private int $butNotMe;
  @java.lang.SuppressWarnings("all") BuilderJavadoc(final int yes, final int getset, final int predef, final int predefWithJavadoc, final List<T> also) {
    super();
    this.yes = yes;
    this.getset = getset;
    this.predef = predef;
    this.predefWithJavadoc = predefWithJavadoc;
    this.also = also;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderJavadocBuilder<T> builder() {
    return new BuilderJavadocBuilder<T>();
  }
  public @java.lang.SuppressWarnings("all") int getGetset() {
    return this.getset;
  }
  public @java.lang.SuppressWarnings("all") void setGetset(final int getset) {
    this.getset = getset;
  }
}
