import java.util.List;
@lombok.Builder(access = lombok.AccessLevel.PROTECTED,setterPrefix = "with") class BuilderWithPrefix<T> {
  protected static @java.lang.SuppressWarnings("all") class BuilderWithPrefixBuilder<T> {
    private @java.lang.SuppressWarnings("all") int unprefixed;
    @java.lang.SuppressWarnings("all") BuilderWithPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderWithPrefixBuilder<T> withUnprefixed(final int unprefixed) {
      this.unprefixed = unprefixed;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithPrefix<T> build() {
      return new BuilderWithPrefix<T>(unprefixed);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderWithPrefix.BuilderWithPrefixBuilder(unprefixed=" + this.unprefixed) + ")");
    }
  }
  private int unprefixed;
  @java.lang.SuppressWarnings("all") BuilderWithPrefix(final int unprefixed) {
    super();
    this.unprefixed = unprefixed;
  }
  protected static @java.lang.SuppressWarnings("all") <T>BuilderWithPrefixBuilder<T> builder() {
    return new BuilderWithPrefixBuilder<T>();
  }
}
