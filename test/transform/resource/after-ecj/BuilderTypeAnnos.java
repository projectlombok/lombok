import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TB {
}
@lombok.Builder class BuilderTypeAnnos {
  public static @java.lang.SuppressWarnings("all") class BuilderTypeAnnosBuilder {
    private @java.lang.SuppressWarnings("all") List<String> foo;
    @java.lang.SuppressWarnings("all") BuilderTypeAnnosBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderTypeAnnos.BuilderTypeAnnosBuilder foo(final @TA List<String> foo) {
      this.foo = foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderTypeAnnos build() {
      return new BuilderTypeAnnos(this.foo);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderTypeAnnos.BuilderTypeAnnosBuilder(foo=" + this.foo) + ")");
    }
  }
  private @TA @TB List<String> foo;
  @java.lang.SuppressWarnings("all") BuilderTypeAnnos(final @TA List<String> foo) {
    super();
    this.foo = foo;
  }
  public static @java.lang.SuppressWarnings("all") BuilderTypeAnnos.BuilderTypeAnnosBuilder builder() {
    return new BuilderTypeAnnos.BuilderTypeAnnosBuilder();
  }
}
