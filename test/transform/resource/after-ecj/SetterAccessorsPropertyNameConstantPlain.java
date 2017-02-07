import lombok.Setter;
import lombok.experimental.Accessors;
class SetterAccessorsPropertyNameConstantPlain {
  public static final java.lang.String PROP_FOO = "foo";
  public static final java.lang.String PROP_CAMEL_CASE = "camelCase";
  @Setter @Accessors(propertyNameConstant = true) int foo;
  @Setter @Accessors(propertyNameConstant = true) String camelCase;
  <clinit>() {
  }
  SetterAccessorsPropertyNameConstantPlain() {
    super();
  }
  public @java.lang.SuppressWarnings("all") void setFoo(final int foo) {
    this.foo = foo;
  }
  public @java.lang.SuppressWarnings("all") void setCamelCase(final String camelCase) {
    this.camelCase = camelCase;
  }
}