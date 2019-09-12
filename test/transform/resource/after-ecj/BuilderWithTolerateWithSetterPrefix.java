import lombok.Builder;
import lombok.experimental.Tolerate;
public @Builder(setterPrefix = "with") class BuilderWithTolerateWithSetterPrefix {
  public static class BuilderWithTolerateWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") int value;
    public @Tolerate BuilderWithTolerateWithSetterPrefixBuilder withValue(String s) {
      return this.withValue(Integer.parseInt(s));
    }
    @java.lang.SuppressWarnings("all") BuilderWithTolerateWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderWithTolerateWithSetterPrefixBuilder withValue(final int value) {
      this.value = value;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithTolerateWithSetterPrefix build() {
      return new BuilderWithTolerateWithSetterPrefix(value);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderWithTolerateWithSetterPrefix.BuilderWithTolerateWithSetterPrefixBuilder(value=" + this.value) + ")");
    }
  }
  private final int value;
  public static void main(String[] args) {
    BuilderWithTolerateWithSetterPrefix.builder().withValue("42").build();
  }
  @java.lang.SuppressWarnings("all") BuilderWithTolerateWithSetterPrefix(final int value) {
    super();
    this.value = value;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithTolerateWithSetterPrefixBuilder builder() {
    return new BuilderWithTolerateWithSetterPrefixBuilder();
  }
}
