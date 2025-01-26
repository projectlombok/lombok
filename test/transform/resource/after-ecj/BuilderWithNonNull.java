@lombok.Builder class BuilderWithNonNull {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithNonNullBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String id;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNullBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNull.BuilderWithNonNullBuilder id(final @lombok.NonNull String id) {
      if ((id == null))
          {
            throw new java.lang.NullPointerException("id is marked non-null but is null");
          }
      this.id = id;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNull build() {
      return new BuilderWithNonNull(this.id);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderWithNonNull.BuilderWithNonNullBuilder(id=" + this.id) + ")");
    }
  }
  private final @lombok.NonNull String id;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNull(final @lombok.NonNull String id) {
    super();
    if ((id == null))
        {
          throw new java.lang.NullPointerException("id is marked non-null but is null");
        }
    this.id = id;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNonNull.BuilderWithNonNullBuilder builder() {
    return new BuilderWithNonNull.BuilderWithNonNullBuilder();
  }
}
