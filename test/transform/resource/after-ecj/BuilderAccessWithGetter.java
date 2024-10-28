import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
public final @Builder(access = AccessLevel.PRIVATE) class BuilderAccessWithGetter {
  private static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderAccessWithGetterBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String string;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAccessWithGetterBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    private @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAccessWithGetter.BuilderAccessWithGetterBuilder string(final String string) {
      this.string = string;
      return this;
    }
    private @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAccessWithGetter build() {
      return new BuilderAccessWithGetter(this.string);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderAccessWithGetter.BuilderAccessWithGetterBuilder(string=" + this.string) + ")");
    }
  }
  private final @Getter String string;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAccessWithGetter(final String string) {
    super();
    this.string = string;
  }
  private static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAccessWithGetter.BuilderAccessWithGetterBuilder builder() {
    return new BuilderAccessWithGetter.BuilderAccessWithGetterBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getString() {
    return this.string;
  }
}
