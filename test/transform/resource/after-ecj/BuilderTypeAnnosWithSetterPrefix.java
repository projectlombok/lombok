import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TB {
}
@lombok.Builder(setterPrefix = "with") class BuilderTypeAnnosWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderTypeAnnosWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated List<String> foo;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderTypeAnnosWithSetterPrefixBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder withFoo(final @TA List<String> foo) {
      this.foo = foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderTypeAnnosWithSetterPrefix build() {
      return new BuilderTypeAnnosWithSetterPrefix(this.foo);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder(foo=" + this.foo) + ")");
    }
  }
  private @TA @TB List<String> foo;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderTypeAnnosWithSetterPrefix(final @TA List<String> foo) {
    super();
    this.foo = foo;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder builder() {
    return new BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder();
  }
}
