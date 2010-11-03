package before;
@lombok.slf4j.Log class LoggerSlf4jWithPackage {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(before.LoggerSlf4jWithPackage.class);
  LoggerSlf4jWithPackage() {
    super();
  }
}
class LoggerSlf4jWithPackageOuter {
  static @lombok.slf4j.Log class Inner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(before.LoggerSlf4jWithPackageOuter.Inner.class);
    Inner() {
      super();
    }
  }
  LoggerSlf4jWithPackageOuter() {
    super();
  }
}