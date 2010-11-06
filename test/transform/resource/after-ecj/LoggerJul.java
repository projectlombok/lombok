@lombok.jul.Log class LoggerJul {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("LoggerJul");
  LoggerJul() {
    super();
  }
}
@lombok.jul.Log(String.class) class LoggerJulString {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("String");
  LoggerJulString() {
    super();
  }
}
@lombok.jul.Log(java.lang.String.class) class LoggerJulJavaLangString {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("java.lang.String");
  LoggerJulJavaLangString() {
    super();
  }
}