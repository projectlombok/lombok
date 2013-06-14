import java.util.List;
@lombok.experimental.Builder class BuilderSimple<T> {
  public static @java.lang.SuppressWarnings("all") class BuilderSimpleBuilder<T> {
    private int yes;
    private List<T> also;
    @java.lang.SuppressWarnings("all") BuilderSimpleBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSimpleBuilder<T> yes(final int yes) {
      this.yes = yes;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSimpleBuilder<T> also(final List<T> also) {
      this.also = also;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSimple<T> build() {
      return new BuilderSimple<T>(yes, also);
    }
  }
  private final int noshow = 0;
  private final int yes;
  private List<T> also;
  private int $butNotMe;
  private @java.lang.SuppressWarnings("all") BuilderSimple(final int yes, final List<T> also) {
    super();
    this.yes = yes;
    this.also = also;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderSimpleBuilder<T> builder() {
    return new BuilderSimpleBuilder<T>();
  }
}
