@lombok.jul.Log class LoggerJul {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerJul.class.getName());
  <clinit>() {
  }
  LoggerJul() {
    super();
  }
}
@lombok.jul.Log(String.class) class LoggerJulString {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(String.class.getName());
  <clinit>() {
  }
  LoggerJulString() {
    super();
  }
}
@lombok.jul.Log(java.lang.String.class) class LoggerJulJavaLangString {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(java.lang.String.class.getName());
  <clinit>() {
  }
  LoggerJulJavaLangString() {
    super();
  }
}