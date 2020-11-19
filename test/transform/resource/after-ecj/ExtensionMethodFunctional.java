import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(value = ExtensionMethodFunctional.Extensions.class,suppressBaseMethods = false) class ExtensionMethodFunctional {
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
    public static <T>List<T> toList(Stream<T> stream) {
      return (List<T>) stream.collect(Collectors.toList());
    }
    public static <T, U>List<U> toList2(Stream<T> stream) {
      return null;
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
    ExtensionMethodFunctional.Extensions.toList(Stream.of("a", "b", "c").map(String::toUpperCase));
    List<Integer> i2 = ExtensionMethodFunctional.Extensions.toList2(Stream.of("a", "b", "c").map(String::toUpperCase));
  }
}
