class ValLambda {
  ValLambda() {
    super();
  }
  public void easyLambda() {
    final @lombok.val java.lang.Runnable foo = (Runnable) () -> {
};
  }
  public void easyIntersectionLambda() {
    final @lombok.val java.lang.Runnable foo = (Runnable & java.io.Serializable) () -> {
};
    final @lombok.val java.io.Serializable bar = (java.io.Serializable & Runnable) () -> {
};
  }
  public void easyLubLambda() {
    final @lombok.val java.lang.Runnable foo = ((System.currentTimeMillis() > 0) ? (Runnable) () -> {
} : System.out::println);
  }
}