@lombok.extern.jul.Log class LoggerJul {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerJul.class.getName());
  <clinit>() {
  }
  LoggerJul() {
    super();
  }
}
@lombok.extern.jul.Log(String.class) class LoggerJulString {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(String.class.getName());
  <clinit>() {
  }
  LoggerJulString() {
    super();
  }
}
@lombok.extern.jul.Log(java.lang.String.class) class LoggerJulJavaLangString {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(java.lang.String.class.getName());
  <clinit>() {
  }
  LoggerJulJavaLangString() {
    super();
  }
}