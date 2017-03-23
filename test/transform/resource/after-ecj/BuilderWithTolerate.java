import lombok.Builder;
import lombok.experimental.Tolerate;
public @Builder class BuilderWithTolerate {
  public static class BuilderWithTolerateBuilder {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int value;
    public @Tolerate BuilderWithTolerateBuilder value(String s) {
      return this.value(Integer.parseInt(s));
    }
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithTolerateBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithTolerateBuilder value(final int value) {
      this.value = value;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithTolerate build() {
      return new BuilderWithTolerate(value);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (("BuilderWithTolerate.BuilderWithTolerateBuilder(value=" + this.value) + ")");
    }
  }
  private final int value;
  public static void main(String[] args) {
    BuilderWithTolerate.builder().value("42").build();
  }
  @java.beans.ConstructorProperties({"value"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithTolerate(final int value) {
    super();
    this.value = value;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithTolerateBuilder builder() {
    return new BuilderWithTolerateBuilder();
  }
}