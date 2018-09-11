import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER}) @interface TA {
}
@lombok.Builder class BuilderTypeAnnos {
  public static @java.lang.SuppressWarnings("all") class BuilderTypeAnnosBuilder {
    private @java.lang.SuppressWarnings("all") List<String> foo;
    @java.lang.SuppressWarnings("all") BuilderTypeAnnosBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderTypeAnnosBuilder foo(final List<String> foo) {
      this.foo = foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderTypeAnnos build() {
      return new BuilderTypeAnnos(foo);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderTypeAnnos.BuilderTypeAnnosBuilder(foo=" + this.foo) + ")");
    }
  }
  private @TA List<@TA String> foo;
  @java.lang.SuppressWarnings("all") BuilderTypeAnnos(final List<String> foo) {
    super();
    this.foo = foo;
  }
  public static @java.lang.SuppressWarnings("all") BuilderTypeAnnosBuilder builder() {
    return new BuilderTypeAnnosBuilder();
  }
}
