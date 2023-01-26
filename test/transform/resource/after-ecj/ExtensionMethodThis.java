import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodThis.Extensions.class) class ExtensionMethodThis {
  static class Extensions {
    Extensions() {
      super();
    }
    public static void hello(ExtensionMethodThis self) {
    }
  }
  ExtensionMethodThis() {
    super();
  }
  public void test() {
    ExtensionMethodThis.Extensions.hello(this);
    hello();
  }
  private void hello() {
  }
}
