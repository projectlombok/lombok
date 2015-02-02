final @lombok.experimental.UtilityClass class UtilityClass {
  protected static class InnerClass {
    private String innerInnerMember;
    protected InnerClass() {
      super();
    }
  }
  private static String someField;
  static void someMethod() {
    System.out.println();
  }
  private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") UtilityClass() {
    super();
    throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
class UtilityInner {
  static class InnerInner {
    static final @lombok.experimental.UtilityClass class InnerInnerInner {
      static int member;
      private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") InnerInnerInner() {
        super();
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
    }
    InnerInner() {
      super();
    }
  }
  UtilityInner() {
    super();
  }
}