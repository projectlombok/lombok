import java.util.List;
@lombok.Builder class BuilderJavadoc<T> {
  public static class BuilderJavadocBuilder<T> {
    private @java.lang.SuppressWarnings("all") int yes;
    private @java.lang.SuppressWarnings("all") int getsetwith;
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
    public @java.lang.SuppressWarnings("all") BuilderJavadocBuilder<T> getsetwith(final int getsetwith) {
      this.getsetwith = getsetwith;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadocBuilder<T> also(final List<T> also) {
      this.also = also;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadoc<T> build() {
      return new BuilderJavadoc<T>(yes, getsetwith, predef, predefWithJavadoc, also);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((((("BuilderJavadoc.BuilderJavadocBuilder(yes=" + this.yes) + ", getsetwith=") + this.getsetwith) + ", predef=") + this.predef) + ", predefWithJavadoc=") + this.predefWithJavadoc) + ", also=") + this.also) + ")");
    }
  }
  private final int noshow = 0;
  private final int yes;
  private @lombok.Getter @lombok.Setter @lombok.experimental.Wither int getsetwith;
  private final int predef;
  private final int predefWithJavadoc;
  private List<T> also;
  private int $butNotMe;
  @java.lang.SuppressWarnings("all") BuilderJavadoc(final int yes, final int getsetwith, final int predef, final int predefWithJavadoc, final List<T> also) {
    super();
    this.yes = yes;
    this.getsetwith = getsetwith;
    this.predef = predef;
    this.predefWithJavadoc = predefWithJavadoc;
    this.also = also;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderJavadocBuilder<T> builder() {
    return new BuilderJavadocBuilder<T>();
  }
  public @java.lang.SuppressWarnings("all") int getGetsetwith() {
    return this.getsetwith;
  }
  public @java.lang.SuppressWarnings("all") void setGetsetwith(final int getsetwith) {
    this.getsetwith = getsetwith;
  }
  public @java.lang.SuppressWarnings("all") BuilderJavadoc<T> withGetsetwith(final int getsetwith) {
    return ((this.getsetwith == getsetwith) ? this : new BuilderJavadoc<T>(this.yes, getsetwith, this.predef, this.predefWithJavadoc, this.also));
  }
}
