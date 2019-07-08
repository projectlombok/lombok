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
}
