import lombok.Setter;
import lombok.experimental.Accessors;
class SetterAccessorsPropertyNameConstantPlain {

  public static final java.lang.String PROP_FOO = "foo";

  @Setter @Accessors(propertyNameConstant = true) int foo;
  <clinit>() {
  }
  SetterAccessorsPropertyNameConstantPlain() {
    super();
  }
  public @java.lang.SuppressWarnings("all") void setFoo(final int foo) {
    this.foo = foo;
  }
}
