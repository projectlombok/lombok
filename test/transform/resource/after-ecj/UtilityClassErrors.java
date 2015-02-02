final @lombok.experimental.UtilityClass class UtilityClassErrors1 {
  private static String someField;
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
