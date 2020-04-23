import java.util.function.Function;
import java.util.function.Consumer;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodFunctional.Extensions.class) class ExtensionMethodFunctional {
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
    public static @SafeVarargs <T>void consume(T o, Consumer<T>... consumer) {
      for (int i = 0;; (i < consumer.length); i ++) 
        {
          consumer[i].accept(o);
        }
    }
  }
  ExtensionMethodFunctional() {
    super();
  }
  public void test() {
    String test = "test";
    test = ExtensionMethodFunctional.Extensions.map(test, (<no type> s) -> ExtensionMethodFunctional.Extensions.reverse(s));
    ExtensionMethodFunctional.Extensions.consume(test, (<no type> s) -> System.out.println(("1: " + s)), (<no type> s) -> System.out.println(("2: " + s)));
    ExtensionMethodFunctional.Extensions.consume(test, System.out::println, System.out::println);
  }
}
