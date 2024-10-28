import java.util.List;
@lombok.Builder(access = lombok.AccessLevel.PROTECTED) class BuilderSimple<T> {
  protected static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderSimpleBuilder<T> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int yes;
    private @java.lang.SuppressWarnings("all") @lombok.Generated List<T> also;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSimpleBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSimple.BuilderSimpleBuilder<T> yes(final int yes) {
      this.yes = yes;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSimple.BuilderSimpleBuilder<T> also(final List<T> also) {
      this.also = also;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSimple<T> build() {
      return new BuilderSimple<T>(this.yes, this.also);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderSimple.BuilderSimpleBuilder(yes=" + this.yes) + ", also=") + this.also) + ")");
    }
  }
  private final int noshow = 0;
  private final int yes;
  private List<T> also;
  private int $butNotMe;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSimple(final int yes, final List<T> also) {
    super();
    this.yes = yes;
    this.also = also;
  }
  protected static @java.lang.SuppressWarnings("all") @lombok.Generated <T>BuilderSimple.BuilderSimpleBuilder<T> builder() {
    return new BuilderSimple.BuilderSimpleBuilder<T>();
  }
}
