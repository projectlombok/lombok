@lombok.log4j.Log class LoggerLog4j {
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoggerLog4j.class);
  LoggerLog4j() {
    super();
  }
}
@lombok.log4j.Log(String.class) class LoggerLog4jString {
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(String.class);
  LoggerLog4jString() {
    super();
  }
}
@lombok.log4j.Log(java.lang.String.class) class LoggerLog4jJavaLangString {
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(java.lang.String.class);
  LoggerLog4jJavaLangString() {
    super();
  }
}