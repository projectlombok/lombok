import java.util.function.Consumer;
import java.util.function.Function;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod({ExtensionMethodAmbiguousFunctional.Extensions.class}) class ExtensionMethodAmbiguousFunctional {
  static class Extensions {
    Extensions() {
      super();
    }
    public static <T, R>void ambiguous(T t, Function<T, R> function) {
    }
    public static <T>void ambiguous(T t, Consumer<T> function) {
    }
  }
  ExtensionMethodAmbiguousFunctional() {
    super();
  }
  public void test() {
    "".ambiguous(System.out::println);
  }
}
