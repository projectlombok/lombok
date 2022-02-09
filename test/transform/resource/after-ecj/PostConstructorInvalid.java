import lombok.experimental.PostConstructor;
abstract class PostConstructorInvalid {
  PostConstructorInvalid() {
    super();
  }
  private @PostConstructor void noConstructor() {
  }
  private static @PostConstructor void staticMethod() {
  }
  public abstract @PostConstructor void abstractMethod();
  private @PostConstructor void withArgument(String arg) {
  }
}