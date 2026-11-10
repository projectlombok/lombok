import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodCacheReceiver.Extensions.class) class ExtensionMethodCacheReceiver {
  static class Foo {
    Foo() {
      super();
    }
  }
  static class Bar {
    Bar() {
      super();
    }
  }
  static class Extensions {
    Extensions() {
      super();
    }
    public static Foo toFoo(String s) {
      return new Foo();
    }
    public static Bar toBar(Foo foo) {
      return new Bar();
    }
    public static Foo again(Bar bar) {
      return new Foo();
    }
    public static int count(Bar bar) {
      return 1;
    }
  }
  String text = "";
  ExtensionMethodCacheReceiver() {
    super();
  }
  public int test() {
    int a = ExtensionMethodCacheReceiver.Extensions.count(ExtensionMethodCacheReceiver.Extensions.toBar(ExtensionMethodCacheReceiver.Extensions.toFoo(text)));
    int b = ExtensionMethodCacheReceiver.Extensions.count(ExtensionMethodCacheReceiver.Extensions.toBar(ExtensionMethodCacheReceiver.Extensions.again(ExtensionMethodCacheReceiver.Extensions.toBar(ExtensionMethodCacheReceiver.Extensions.toFoo("x")))));
    int c = Math.max(ExtensionMethodCacheReceiver.Extensions.count(ExtensionMethodCacheReceiver.Extensions.toBar(ExtensionMethodCacheReceiver.Extensions.toFoo(this.text))), ExtensionMethodCacheReceiver.Extensions.count(ExtensionMethodCacheReceiver.Extensions.toBar(ExtensionMethodCacheReceiver.Extensions.again(ExtensionMethodCacheReceiver.Extensions.toBar(ExtensionMethodCacheReceiver.Extensions.toFoo(text))))));
    return (((a + b) + c) + ExtensionMethodCacheReceiver.Extensions.count(ExtensionMethodCacheReceiver.Extensions.toBar(ExtensionMethodCacheReceiver.Extensions.again(ExtensionMethodCacheReceiver.Extensions.toBar(ExtensionMethodCacheReceiver.Extensions.again(ExtensionMethodCacheReceiver.Extensions.toBar(ExtensionMethodCacheReceiver.Extensions.toFoo(text))))))));
  }
}
