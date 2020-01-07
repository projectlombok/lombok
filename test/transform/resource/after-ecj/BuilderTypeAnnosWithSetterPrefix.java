import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TB {
}
@lombok.Builder(setterPrefix = "with") class BuilderTypeAnnosWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") class BuilderTypeAnnosWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") List<String> foo;
    @java.lang.SuppressWarnings("all") BuilderTypeAnnosWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder withFoo(final @TA List<String> foo) {
      this.foo = foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderTypeAnnosWithSetterPrefix build() {
      return new BuilderTypeAnnosWithSetterPrefix(this.foo);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder(foo=" + this.foo) + ")");
    }
  }
  private @TA @TB List<String> foo;
  @java.lang.SuppressWarnings("all") BuilderTypeAnnosWithSetterPrefix(final @TA List<String> foo) {
    super();
    this.foo = foo;
  }
  public static @java.lang.SuppressWarnings("all") BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder builder() {
    return new BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder();
  }
}
