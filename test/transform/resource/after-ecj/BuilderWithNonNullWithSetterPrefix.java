@lombok.Builder(setterPrefix = "with") class BuilderWithNonNullWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") class BuilderWithNonNullWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") String id;
    @java.lang.SuppressWarnings("all") BuilderWithNonNullWithSetterPrefixBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder withId(final @lombok.NonNull String id) {
      if ((id == null))
          {
            throw new java.lang.NullPointerException("id is marked non-null but is null");
          }
      this.id = id;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithNonNullWithSetterPrefix build() {
      return new BuilderWithNonNullWithSetterPrefix(this.id);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder(id=" + this.id) + ")");
    }
  }
  private final @lombok.NonNull String id;
  @java.lang.SuppressWarnings("all") BuilderWithNonNullWithSetterPrefix(final @lombok.NonNull String id) {
    super();
    if ((id == null))
        {
          throw new java.lang.NullPointerException("id is marked non-null but is null");
        }
    this.id = id;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder builder() {
    return new BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder();
  }
}
