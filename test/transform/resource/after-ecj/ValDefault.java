interface ValDefault {
  int size();
  default void method() {
    final @lombok.val int x = 1;
    final @lombok.val int size = size();
  }
}