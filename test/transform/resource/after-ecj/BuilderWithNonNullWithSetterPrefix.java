@lombok.Builder(setterPrefix = "with") class BuilderWithNonNullWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithNonNullWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String id;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNullWithSetterPrefixBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder withId(final @lombok.NonNull String id) {
      if ((id == null))
          {
            throw new java.lang.NullPointerException("id is marked non-null but is null");
          }
      this.id = id;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNullWithSetterPrefix build() {
      return new BuilderWithNonNullWithSetterPrefix(this.id);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder(id=" + this.id) + ")");
    }
  }
  private final @lombok.NonNull String id;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNullWithSetterPrefix(final @lombok.NonNull String id) {
    super();
    if ((id == null))
        {
          throw new java.lang.NullPointerException("id is marked non-null but is null");
        }
    this.id = id;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder builder() {
    return new BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder();
  }
}
