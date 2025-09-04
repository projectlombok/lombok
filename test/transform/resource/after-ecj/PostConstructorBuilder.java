import lombok.Builder;
import lombok.experimental.PostConstructor;
@Builder class PostConstructorBuilder {
  public static @java.lang.SuppressWarnings("all") class PostConstructorBuilderBuilder {
    private @java.lang.SuppressWarnings("all") String field;
    @java.lang.SuppressWarnings("all") PostConstructorBuilderBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") PostConstructorBuilder.PostConstructorBuilderBuilder field(final String field) {
      this.field = field;
      return this;
    }
    public @java.lang.SuppressWarnings("all") PostConstructorBuilder build() {
      return new PostConstructorBuilder(this.field);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("PostConstructorBuilder.PostConstructorBuilderBuilder(field=" + this.field) + ")");
    }
  }
  private String field;
  private @PostConstructor void postConstructor() {
  }
  @java.lang.SuppressWarnings("all") PostConstructorBuilder(final String field) {
    super();
    this.field = field;
    postConstructor();
  }
  public static @java.lang.SuppressWarnings("all") PostConstructorBuilder.PostConstructorBuilderBuilder builder() {
    return new PostConstructorBuilder.PostConstructorBuilderBuilder();
  }
}
