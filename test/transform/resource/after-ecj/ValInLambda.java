// version 8:

import lombok.val;
class ValInLambda {
  Runnable foo = (Runnable) () -> {
  final @val int i = 1;
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
}
