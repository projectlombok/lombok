class ExtensionMethodNonStaticAccess {
  ExtensionMethodNonStaticAccess() {
    super();
  }
  public void method() {
    Derived derived = new Derived();
    derived.staticMethod();
  }
}
class Base {
  Base() {
    super();
  }
  static String staticMethod() {
    return "";
  }
}
class Derived extends Base {
  Derived() {
    super();
  }
}
