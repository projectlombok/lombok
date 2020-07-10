import java.io.Serializable;

class ValLambda {
  static {
    final @lombok.val java.lang.Runnable foo = ((System.currentTimeMillis() > 0) ? (Runnable) () -> {
} : System.out::println);
  }
  {
    final @lombok.val java.lang.Runnable foo = ((System.currentTimeMillis() > 0) ? (Runnable) () -> {
} : System.out::println);
  }
  <clinit>() {
  }
  ValLambda() {
    super();
  }
  public void easyLambda() {
    final @lombok.val java.lang.Runnable foo = (Runnable) () -> {
};
  }
  public void intersectionLambda() {
    final @lombok.val java.io.Serializable foo = (Runnable & Serializable) () -> {
};
    final @lombok.val java.io.Serializable bar = (java.io.Serializable & Runnable) () -> {
};
  }
  public void easyLubLambda() {
    final @lombok.val java.lang.Runnable foo = ((System.currentTimeMillis() > 0) ? (Runnable) () -> {
} : System.out::println);
    final @lombok.val java.lang.Runnable foo1 = ((System.currentTimeMillis() > 0) ? (Runnable) System.out::println : System.out::println);
    final @lombok.val java.util.function.Function foo2 = ((System.currentTimeMillis() < 0) ? (java.util.function.Function) (<no type> r) -> "" : (<no type> r) -> System.currentTimeMillis());
    java.util.function.Function foo3 = ((System.currentTimeMillis() < 0) ? (java.util.function.Function) (<no type> r) -> "" : (<no type> r) -> System.currentTimeMillis());
    final @lombok.val java.util.function.Function<java.lang.String, java.lang.String> foo4 = ((System.currentTimeMillis() < 0) ? (java.util.function.Function<String, String>) (<no type> r) -> "" : (<no type> r) -> String.valueOf(System.currentTimeMillis()));
  }
}