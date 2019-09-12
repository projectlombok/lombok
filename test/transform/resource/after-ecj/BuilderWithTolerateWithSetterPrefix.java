import lombok.Builder;
import lombok.experimental.Tolerate;
public @Builder(setterPrefix = "with") class BuilderWithTolerate {
  public static class BuilderWithTolerateBuilder {
    private @java.lang.SuppressWarnings("all") int value;
    public @Tolerate BuilderWithTolerateBuilder withValue(String s) {
      return this.withValue(Integer.parseInt(s));
    }
    @java.lang.SuppressWarnings("all") BuilderWithTolerateBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderWithTolerateBuilder withValue(final int value) {
      this.value = value;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithTolerate build() {
      return new BuilderWithTolerate(value);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderWithTolerate.BuilderWithTolerateBuilder(value=" + this.value) + ")");
    }
  }
  private final int value;
  public static void main(String[] args) {
    BuilderWithTolerate.builder().withValue("42").build();
  }
  @java.lang.SuppressWarnings("all") BuilderWithTolerate(final int value) {
    super();
    this.value = value;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithTolerateBuilder builder() {
    return new BuilderWithTolerateBuilder();
  }
}
