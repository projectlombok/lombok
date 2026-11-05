import java.util.List;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodCacheGeneric.Extensions.class) class ExtensionMethodCacheGeneric {
  static class Extensions {
    Extensions() {
      super();
    }
    public static <T>T first(List<T> list) {
      return (list.isEmpty() ? null : list.get(0));
    }
    public static <T>List<T> wrap(T value) {
      return null;
    }
    public static String shout(String s) {
      return s;
    }
    public static Integer len(String s) {
      return s.length();
    }
    public static Integer twice(Number n) {
      return (n.intValue() * 2);
    }
  }
  ExtensionMethodCacheGeneric() {
    super();
  }
  public void test() {
    List<String> strings = null;
    List<Number> numbers = null;
    ExtensionMethodCacheGeneric.Extensions.len(ExtensionMethodCacheGeneric.Extensions.shout("a"));
    ExtensionMethodCacheGeneric.Extensions.shout(ExtensionMethodCacheGeneric.Extensions.first(strings));
    ExtensionMethodCacheGeneric.Extensions.len(ExtensionMethodCacheGeneric.Extensions.shout(ExtensionMethodCacheGeneric.Extensions.first(strings)));
    ExtensionMethodCacheGeneric.Extensions.twice(ExtensionMethodCacheGeneric.Extensions.first(numbers));
    ExtensionMethodCacheGeneric.Extensions.shout(ExtensionMethodCacheGeneric.Extensions.first(ExtensionMethodCacheGeneric.Extensions.wrap(ExtensionMethodCacheGeneric.Extensions.shout("b"))));
    ExtensionMethodCacheGeneric.Extensions.first(ExtensionMethodCacheGeneric.Extensions.wrap(ExtensionMethodCacheGeneric.Extensions.twice(ExtensionMethodCacheGeneric.Extensions.len(ExtensionMethodCacheGeneric.Extensions.first(strings)))));
    ExtensionMethodCacheGeneric.Extensions.shout("c");
    ExtensionMethodCacheGeneric.Extensions.twice(ExtensionMethodCacheGeneric.Extensions.twice(ExtensionMethodCacheGeneric.Extensions.first(numbers)));
  }
}
