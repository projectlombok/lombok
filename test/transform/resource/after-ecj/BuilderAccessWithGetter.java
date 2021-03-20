import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
public final @Builder(access = AccessLevel.PRIVATE) class BuilderAccessWithGetter {
  private static @java.lang.SuppressWarnings("all") class BuilderAccessWithGetterBuilder {
    private @java.lang.SuppressWarnings("all") String string;
    @java.lang.SuppressWarnings("all") BuilderAccessWithGetterBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    private @java.lang.SuppressWarnings("all") BuilderAccessWithGetter.BuilderAccessWithGetterBuilder string(final String string) {
      this.string = string;
      return this;
    }
    private @java.lang.SuppressWarnings("all") BuilderAccessWithGetter build() {
      return new BuilderAccessWithGetter(this.string);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderAccessWithGetter.BuilderAccessWithGetterBuilder(string=" + this.string) + ")");
    }
  }
  private final @Getter String string;
  @java.lang.SuppressWarnings("all") BuilderAccessWithGetter(final String string) {
    super();
    this.string = string;
  }
  private static @java.lang.SuppressWarnings("all") BuilderAccessWithGetter.BuilderAccessWithGetterBuilder builder() {
    return new BuilderAccessWithGetter.BuilderAccessWithGetterBuilder();
  }
  public @java.lang.SuppressWarnings("all") String getString() {
    return this.string;
  }
}
