final @lombok.experimental.UtilityClass class UtilityClassErrors1 {
  private static String someField;
  <clinit>() {
  }
  protected UtilityClassErrors1() {
    super();
  }
  static void method() {
    @lombok.experimental.UtilityClass class MethodLocalClass {
      MethodLocalClass() {
        super();
      }
    }
  }
}
@lombok.experimental.UtilityClass enum UtilityClassErrors2 {
  <clinit>() {
  }
  UtilityClassErrors2() {
    super();
  }
}
class UtilityClassErrors3 {
  class NonStaticInner {
    @lombok.experimental.UtilityClass class ThisShouldFail {
      private String member;
      ThisShouldFail() {
        super();
      }
    }
    NonStaticInner() {
      super();
    }
  }
  UtilityClassErrors3() {
    super();
  }
}
