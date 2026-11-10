// version 10:
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodCacheStatements.Extensions.class) class ExtensionMethodCacheStatements {
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
  static final int TOTAL;
  static {
    var a = ExtensionMethodCacheStatements.Extensions.toFoo("a");
    var b = ExtensionMethodCacheStatements.Extensions.toBar(a);
    var c = ExtensionMethodCacheStatements.Extensions.again(b);
    var d = ExtensionMethodCacheStatements.Extensions.toBar(c);
    TOTAL = (ExtensionMethodCacheStatements.Extensions.count(d) + ExtensionMethodCacheStatements.Extensions.count(ExtensionMethodCacheStatements.Extensions.toBar(a)));
  }
  static Function<String, Integer> function = (<no type> x) ->   {
    var f = ExtensionMethodCacheStatements.Extensions.toFoo(x);
    return ExtensionMethodCacheStatements.Extensions.count(ExtensionMethodCacheStatements.Extensions.toBar(f));
  };
  static Supplier<Integer> supplier = () ->   {
    var f = ExtensionMethodCacheStatements.Extensions.toFoo("x");
    var b = ExtensionMethodCacheStatements.Extensions.toBar(f);
    return ExtensionMethodCacheStatements.Extensions.count(b);
  };
  int field = ExtensionMethodCacheStatements.Extensions.count(ExtensionMethodCacheStatements.Extensions.toBar(ExtensionMethodCacheStatements.Extensions.toFoo("a")));
  <clinit>() {
  }
  ExtensionMethodCacheStatements() {
    super();
  }
  public int test(String s) {
    var a = ExtensionMethodCacheStatements.Extensions.toFoo(s);
    var b = ExtensionMethodCacheStatements.Extensions.toBar(a);
    var c = ExtensionMethodCacheStatements.Extensions.again(b);
    var d = ExtensionMethodCacheStatements.Extensions.toBar(c);
    var e = ExtensionMethodCacheStatements.Extensions.again(d);
    var f = ExtensionMethodCacheStatements.Extensions.toBar(e);
    return ((ExtensionMethodCacheStatements.Extensions.count(f) + ExtensionMethodCacheStatements.Extensions.count(d)) + ExtensionMethodCacheStatements.Extensions.count(b));
  }
}
