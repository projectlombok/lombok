@lombok.commons.Log class LoggerCommons {
  private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggerCommons.class);
  <clinit>() {
  }
  LoggerCommons() {
    super();
  }
}
@lombok.commons.Log(String.class) class LoggerCommonsString {
  private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(String.class);
  <clinit>() {
  }
  LoggerCommonsString() {
    super();
  }
}
@lombok.commons.Log(java.lang.String.class) class LoggerCommonsJavaLangString {
  private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(java.lang.String.class);
  <clinit>() {
  }
  LoggerCommonsJavaLangString() {
    super();
  }
}