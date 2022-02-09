import lombok.experimental.PostConstructor;
abstract class PostConstructorInvalid {
  private static @PostConstructor void staticMethod() {
  }
  public abstract @PostConstructor void abstractMethod();
  private @PostConstructor void withArgument(String arg) {
  }
  public @java.lang.SuppressWarnings("all") PostConstructorInvalid() {
    super();
  }
}
