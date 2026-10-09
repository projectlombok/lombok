import lombok.NonNull;
class NonNullTypeUse {
  NonNullTypeUse() {
    super();
  }
  void test1(@NonNull String[][][] args) {
  }
  void test2(String @NonNull [][][] args) {
    if ((args == null))
        {
          throw new java.lang.NullPointerException("args is marked non-null but is null");
        }
  }
  void test3(String[] @NonNull [][] args) {
  }
  void test4(String[][] @NonNull [] args) {
  }
  void test5(@NonNull String simple) {
    if ((simple == null))
        {
          throw new java.lang.NullPointerException("simple is marked non-null but is null");
        }
  }
  void test6(java.lang.@NonNull String weird) {
    if ((weird == null))
        {
          throw new java.lang.NullPointerException("weird is marked non-null but is null");
        }
  }
  void test7(java.lang.String @NonNull [][] weird) {
    if ((weird == null))
        {
          throw new java.lang.NullPointerException("weird is marked non-null but is null");
        }
  }
  void test8(@NonNull String... args) {
  }
  void test9(String @NonNull ... args) {
    if ((args == null))
        {
          throw new java.lang.NullPointerException("args is marked non-null but is null");
        }
  }
  void test10(java.lang.@NonNull String[] elements) {
  }
  void test11(String[] @NonNull ... elements) {
  }
  void test12(java.util.List<@NonNull String> elements) {
  }
  void test13(@NonNull String @NonNull [] args) {
    if ((args == null))
        {
          throw new java.lang.NullPointerException("args is marked non-null but is null");
        }
  }
  void test14(@NonNull String[] @NonNull [] elements) {
  }
  void test15(java.util. @NonNull List<String> args) {
    if ((args == null))
        {
          throw new java.lang.NullPointerException("args is marked non-null but is null");
        }
  }
}
