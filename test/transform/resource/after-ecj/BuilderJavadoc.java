import java.util.List;
@lombok.Builder class BuilderJavadoc<T> {
  public static class BuilderJavadocBuilder<T> {
    private @java.lang.SuppressWarnings("all") int basic;
    private @java.lang.SuppressWarnings("all") int getsetwith;
    private @java.lang.SuppressWarnings("all") int predef;
    private @java.lang.SuppressWarnings("all") int predefWithJavadoc;
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
    /**
     * basic gets only a builder setter.
     * @see #getsetwith
     * @param tag is moved to the setter.
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderJavadoc.BuilderJavadocBuilder<T> basic(final int basic) {
      this.basic = basic;
      return this;
    }
    /**
     * getsetwith gets a builder setter, an instance getter and setter, and a wither.
     * @param tag is moved to the setters and wither.
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderJavadoc.BuilderJavadocBuilder<T> getsetwith(final int getsetwith) {
      this.getsetwith = getsetwith;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadoc<T> build() {
      return new BuilderJavadoc<T>(this.basic, this.getsetwith, this.predef, this.predefWithJavadoc);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderJavadoc.BuilderJavadocBuilder(basic=" + this.basic) + ", getsetwith=") + this.getsetwith) + ", predef=") + this.predef) + ", predefWithJavadoc=") + this.predefWithJavadoc) + ")");
    }
  }
  private final int basic;
  private @lombok.Getter @lombok.Setter @lombok.experimental.Wither int getsetwith;
  private final int predef;
  private final int predefWithJavadoc;
  @java.lang.SuppressWarnings("all") BuilderJavadoc(final int basic, final int getsetwith, final int predef, final int predefWithJavadoc) {
    super();
    this.basic = basic;
    this.getsetwith = getsetwith;
    this.predef = predef;
    this.predefWithJavadoc = predefWithJavadoc;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderJavadoc.BuilderJavadocBuilder<T> builder() {
    return new BuilderJavadoc.BuilderJavadocBuilder<T>();
  }
  /**
   * getsetwith gets a builder setter, an instance getter and setter, and a wither.
   * @return tag is moved to the getter.
   */
  public @java.lang.SuppressWarnings("all") int getGetsetwith() {
    return this.getsetwith;
  }
  /**
   * getsetwith gets a builder setter, an instance getter and setter, and a wither.
   * @param tag is moved to the setters and wither.
   */
  public @java.lang.SuppressWarnings("all") void setGetsetwith(final int getsetwith) {
    this.getsetwith = getsetwith;
  }
  /**
   * getsetwith gets a builder setter, an instance getter and setter, and a wither.
   * @param tag is moved to the setters and wither.
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") BuilderJavadoc<T> withGetsetwith(final int getsetwith) {
    return ((this.getsetwith == getsetwith) ? this : new BuilderJavadoc<T>(this.basic, getsetwith, this.predef, this.predefWithJavadoc));
  }
}
