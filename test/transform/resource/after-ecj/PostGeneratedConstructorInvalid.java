import lombok.experimental.PostGeneratedConstructor;
abstract class PostGeneratedConstructorInvalid {
  PostGeneratedConstructorInvalid() {
    super();
  }
  private @PostGeneratedConstructor void noConstructor() {
  }
  private static @PostGeneratedConstructor void staticMethod() {
  }
  public abstract @PostGeneratedConstructor void abstractMethod();
  private @PostGeneratedConstructor void withArgument(String arg) {
  }
}