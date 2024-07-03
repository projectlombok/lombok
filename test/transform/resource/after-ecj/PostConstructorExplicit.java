import lombok.experimental.PostConstructor;
class PostConstructorExplicit {
  private @PostConstructor void postConstructor1() {
  }
  private @PostConstructor void postConstructor2() {
  }
  public PostConstructorExplicit(long a) {
    this();
  }
  public @PostConstructor.SkipPostConstructors PostConstructorExplicit(int a) {
    super();
  }
  public @PostConstructor.InvokePostConstructors PostConstructorExplicit(short a) {
    super();
    postConstructor1();
    postConstructor2();
  }
  public @PostConstructor.SkipPostConstructors @PostConstructor.InvokePostConstructors PostConstructorExplicit(byte a) {
    super();
  }
  public @PostConstructor.InvokePostConstructors PostConstructorExplicit(boolean a) {
    this();
    postConstructor1();
    postConstructor2();
  }
  public @PostConstructor.SkipPostConstructors PostConstructorExplicit(double a) {
    this();
  }
  public PostConstructorExplicit() {
    super();
  }
}
