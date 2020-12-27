import java.util.List;
@lombok.Builder(access = lombok.AccessLevel.PROTECTED,setterPrefix = "with") class BuilderSimpleWithSetterPrefix<T> {
  protected static @java.lang.SuppressWarnings("all") class BuilderSimpleWithSetterPrefixBuilder<T> {
    private @java.lang.SuppressWarnings("all") int unprefixed;
    @java.lang.SuppressWarnings("all") BuilderSimpleWithSetterPrefixBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderSimpleWithSetterPrefix.BuilderSimpleWithSetterPrefixBuilder<T> withUnprefixed(final int unprefixed) {
      this.unprefixed = unprefixed;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSimpleWithSetterPrefix<T> build() {
      return new BuilderSimpleWithSetterPrefix<T>(this.unprefixed);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderSimpleWithSetterPrefix.BuilderSimpleWithSetterPrefixBuilder(unprefixed=" + this.unprefixed) + ")");
    }
  }
  private int unprefixed;
  @java.lang.SuppressWarnings("all") BuilderSimpleWithSetterPrefix(final int unprefixed) {
    super();
    this.unprefixed = unprefixed;
  }
  protected static @java.lang.SuppressWarnings("all") <T>BuilderSimpleWithSetterPrefix.BuilderSimpleWithSetterPrefixBuilder<T> builder() {
    return new BuilderSimpleWithSetterPrefix.BuilderSimpleWithSetterPrefixBuilder<T>();
  }
}
