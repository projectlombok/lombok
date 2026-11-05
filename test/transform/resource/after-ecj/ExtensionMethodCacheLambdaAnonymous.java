import java.util.function.Function;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodCacheLambdaAnonymous.Extensions.class) class ExtensionMethodCacheLambdaAnonymous {
  static class Base {
    Base() {
      super();
    }
    Number get() {
      return 1;
    }
  }
  static class Extensions {
    Extensions() {
      super();
    }
    public static String get(Base base) {
      return "";
    }
    public static String shout(String s) {
      return s;
    }
    public static Integer length(String s) {
      return ExtensionMethodCacheLambdaAnonymous.Extensions.length(s);
    }
  }
  ExtensionMethodCacheLambdaAnonymous() {
    super();
  }
  public void test() {
    Base base = new Base();
    String s = "a";
    ExtensionMethodCacheLambdaAnonymous.Extensions.shout(s);
    Function<String, String> f = (<no type> t) -> ExtensionMethodCacheLambdaAnonymous.Extensions.shout(t);
    Function<Base, String> g = (<no type> b) -> ExtensionMethodCacheLambdaAnonymous.Extensions.shout(ExtensionMethodCacheLambdaAnonymous.Extensions.get(b));
    Runnable r = new Runnable() {
      x() {
        super();
      }
      public void run() {
        String u = "b";
        ExtensionMethodCacheLambdaAnonymous.Extensions.shout(ExtensionMethodCacheLambdaAnonymous.Extensions.shout(u));
        ExtensionMethodCacheLambdaAnonymous.Extensions.shout(ExtensionMethodCacheLambdaAnonymous.Extensions.get(base));
      }
      void other() {
        ExtensionMethodCacheLambdaAnonymous.Extensions.shout("c");
      }
    };
    ExtensionMethodCacheLambdaAnonymous.Extensions.shout(s);
    ExtensionMethodCacheLambdaAnonymous.Extensions.shout(ExtensionMethodCacheLambdaAnonymous.Extensions.get(base));
    Function<Base, Integer> h = (<no type> b) -> ExtensionMethodCacheLambdaAnonymous.Extensions.length(ExtensionMethodCacheLambdaAnonymous.Extensions.get(b));
    ExtensionMethodCacheLambdaAnonymous.Extensions.shout(s);
  }
}
