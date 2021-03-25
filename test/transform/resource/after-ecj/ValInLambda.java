import java.util.function.Function;
import java.util.function.Supplier;
import lombok.val;
class ValInLambda {
  Runnable foo = (Runnable) () -> {
  final @val int i = 1;
  final @lombok.val java.lang.Runnable foo = ((System.currentTimeMillis() > 0) ? (Runnable) () -> {
} : System.out::println);
};
  ValInLambda() {
    super();
  }
  public void easyLambda() {
    Runnable foo = (Runnable) () -> {
  final @val int i = 1;
};
  }
  public void easyIntersectionLambda() {
    Runnable foo = (Runnable) () -> {
  final @val int i = 1;
};
  }
  public void easyLubLambda() {
    Runnable foo = (Runnable) () -> {
  final @lombok.val java.lang.Runnable fooInner = ((System.currentTimeMillis() > 0) ? (Runnable) () -> {
} : System.out::println);
};
  }
  public void inParameter() {
    final @val java.util.function.Function<java.util.function.Supplier<java.lang.String>, java.lang.String> foo = (Function<Supplier<String>, String>) (<no type> s) -> s.get();
    final @val java.lang.String foo2 = foo.apply(() -> {
  final @val java.lang.String bar = "";
  return bar;
});
  }
}
