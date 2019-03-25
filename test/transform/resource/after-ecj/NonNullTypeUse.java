import lombok.NonNull;
class NonNullTypeUse {
  NonNullTypeUse() {
    super();
  }
  void test1(@NonNull String[][][] args) {
    if ((args == null))
        {
          throw new java.lang.NullPointerException("args is marked non-null but is null");
        }
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
}