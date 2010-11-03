@lombok.slf4j.Log class LoggerSlf4j {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4j.class);
  LoggerSlf4j() {
    super();
  }
}
class LoggerSlf4jOuter {
  static @lombok.slf4j.Log class Inner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jOuter.Inner.class);
    Inner() {
      super();
    }
  }
  LoggerSlf4jOuter() {
    super();
  }
}