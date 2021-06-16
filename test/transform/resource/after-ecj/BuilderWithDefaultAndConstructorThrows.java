import lombok.Builder;
import java.io.IOException;
public @Builder class BuilderWithDefaultAndConstructorThrows {
  public static @java.lang.SuppressWarnings("all") class BuilderWithDefaultAndConstructorThrowsBuilder {
    private @java.lang.SuppressWarnings("all") int value;
    private @java.lang.SuppressWarnings("all") int value$value;
    private @java.lang.SuppressWarnings("all") boolean value$set;
    @java.lang.SuppressWarnings("all") BuilderWithDefaultAndConstructorThrowsBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithDefaultAndConstructorThrows.BuilderWithDefaultAndConstructorThrowsBuilder value(final int value) {
      this.value = value;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithDefaultAndConstructorThrows build() throws IOException {
      return new BuilderWithDefaultAndConstructorThrows(this.value);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderWithDefaultAndConstructorThrows.BuilderWithDefaultAndConstructorThrowsBuilder(value=" + this.value) + ")");
    }
  }
  private final @Builder.Default int value;
  public @Builder BuilderWithDefaultAndConstructorThrows(int value) throws IOException {
    super();
    if ((value > 100))
        {
          throw new IOException("value too large");
        }
    this.value = value;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithDefaultAndConstructorThrows.BuilderWithDefaultAndConstructorThrowsBuilder builder() {
    return new BuilderWithDefaultAndConstructorThrows.BuilderWithDefaultAndConstructorThrowsBuilder();
  }
  private static @java.lang.SuppressWarnings("all") int $default$value() {
    return 10;
  }
}
