import java.util.function.Function;
import lombok.experimental.ExtensionMethod;
public @ExtensionMethod(value = ExtensionMethodInLambda.Extensions.class) class ExtensionMethodInLambda {
  static class Extensions {
    Extensions() {
      super();
    }
    public static <T, R>R map(T value, Function<T, R> mapper) {
      return mapper.apply(value);
    }
    public static String reverse(String string) {
      return new StringBuilder(string).reverse().toString();
    }
    public static String trim(Integer integer) {
      return "0";
    }
  }
  public ExtensionMethodInLambda() {
    super();
  }
  public void testSimple() {
    String test = "test";
    test = ExtensionMethodInLambda.Extensions.map(test, (<no type> s) -> ExtensionMethodInLambda.Extensions.reverse(s));
  }
  public void testSameName() {
    String test = "test";
    test = ExtensionMethodInLambda.Extensions.map(test, (<no type> s) -> s.trim());
  }
  public void testArgumentOfInvalidMethod() {
    String test = "test";
    test.invalid((<no type> s) -> s.reverse());
  }
}
