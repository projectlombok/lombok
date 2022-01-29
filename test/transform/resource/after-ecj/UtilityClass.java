final @lombok.experimental.UtilityClass class UtilityClass {
  protected static class InnerClass {
    private String innerInnerMember;
    protected InnerClass() {
      super();
    }
  }
  protected static class InnerStaticClass {
    private String innerInnerMember;
    protected InnerStaticClass() {
      super();
    }
  }
  private static long someField = System.currentTimeMillis();
  <clinit>() {
  }
  static void someMethod() {
    System.out.println();
    new InnerClass();
    new InnerStaticClass();
  }
  private @java.lang.SuppressWarnings("all") UtilityClass() {
    super();
    throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
class UtilityInner {
  static class InnerInner {
    static final @lombok.experimental.UtilityClass class InnerInnerInner {
      static int member;
      <clinit>() {
      }
      private @java.lang.SuppressWarnings("all") InnerInnerInner() {
        super();
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
    }
    InnerInner() {
      super();
    }
  }
  enum UtilityInsideEnum {
    static final @lombok.experimental.UtilityClass class InsideEnum {
      static int member;
      <clinit>() {
      }
      private @java.lang.SuppressWarnings("all") InsideEnum() {
        super();
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
    }
    FOO(),
    BAR(),
    <clinit>() {
    }
    UtilityInsideEnum() {
      super();
    }
  }
  interface UtilityInsideInterface {
    final @lombok.experimental.UtilityClass class InsideInterface {
      static int member;
      <clinit>() {
      }
      private @java.lang.SuppressWarnings("all") InsideInterface() {
        super();
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
    }
  }
  UtilityInner() {
    super();
  }
}