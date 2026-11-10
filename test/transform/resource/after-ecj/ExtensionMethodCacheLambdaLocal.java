// version 10:
import java.util.List;
import java.util.Optional;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodCacheLambdaLocal.Extensions.class) class ExtensionMethodCacheLambdaLocal {
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
    public static Bar toBar(String s) {
      return new Bar();
    }
    public static int count(Bar bar) {
      return 1;
    }
  }
  ExtensionMethodCacheLambdaLocal() {
    super();
  }
  public int test(List<String> list, Optional<String> opt) {
    int a = list.stream().mapToInt((<no type> x) -> ExtensionMethodCacheLambdaLocal.Extensions.count(ExtensionMethodCacheLambdaLocal.Extensions.toBar(ExtensionMethodCacheLambdaLocal.Extensions.toFoo(x)))).sum();
    int b = opt.map((<no type> x) -> ExtensionMethodCacheLambdaLocal.Extensions.toFoo(x)).map((<no type> f) -> ExtensionMethodCacheLambdaLocal.Extensions.toBar(f)).map((<no type> r) -> ExtensionMethodCacheLambdaLocal.Extensions.count(r)).orElse(0);
    int c = Optional.of(ExtensionMethodCacheLambdaLocal.Extensions.toFoo("x")).map((<no type> f) -> ExtensionMethodCacheLambdaLocal.Extensions.count(ExtensionMethodCacheLambdaLocal.Extensions.toBar(f))).orElse(0);
    int d = list.stream().mapToInt((<no type> x) -> {
  var f = ExtensionMethodCacheLambdaLocal.Extensions.toFoo(x);
  var r = ExtensionMethodCacheLambdaLocal.Extensions.toBar(f);
  return ExtensionMethodCacheLambdaLocal.Extensions.count(r);
}).sum();
    Optional<Integer> e = Optional.of(ExtensionMethodCacheLambdaLocal.Extensions.toFoo("x")).map((<no type> f) -> (ExtensionMethodCacheLambdaLocal.Extensions.count(ExtensionMethodCacheLambdaLocal.Extensions.toBar(f)) + ExtensionMethodCacheLambdaLocal.Extensions.count(ExtensionMethodCacheLambdaLocal.Extensions.toBar("lit"))));
    return ((((a + b) + c) + d) + e.get());
  }
}
