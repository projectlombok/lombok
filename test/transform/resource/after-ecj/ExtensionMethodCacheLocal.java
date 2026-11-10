// version 10:
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodCacheLocal.Extensions.class) class ExtensionMethodCacheLocal {
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
  ExtensionMethodCacheLocal() {
    super();
  }
  public int test(String s) {
    Foo f = ExtensionMethodCacheLocal.Extensions.toFoo(s);
    Bar b = ExtensionMethodCacheLocal.Extensions.toBar(f);
    var g = ExtensionMethodCacheLocal.Extensions.toFoo(s);
    var c = ExtensionMethodCacheLocal.Extensions.toBar(g);
    int n = (ExtensionMethodCacheLocal.Extensions.count(c) + ExtensionMethodCacheLocal.Extensions.count(b));
    Foo h;
    h = ExtensionMethodCacheLocal.Extensions.again(c);
    var d = ExtensionMethodCacheLocal.Extensions.toBar(h);
    if ((n > 0))
        {
          var e = ExtensionMethodCacheLocal.Extensions.again(d);
          n += ExtensionMethodCacheLocal.Extensions.count(ExtensionMethodCacheLocal.Extensions.toBar(e));
        }
    return (n + ExtensionMethodCacheLocal.Extensions.count(ExtensionMethodCacheLocal.Extensions.toBar(h)));
  }
}
