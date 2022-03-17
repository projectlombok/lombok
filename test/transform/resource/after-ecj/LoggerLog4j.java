import lombok.extern.log4j.Log4j;
@lombok.extern.log4j.Log4j class LoggerLog4j {
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoggerLog4j.class);
  <clinit>() {
  }
  LoggerLog4j() {
    super();
  }
}
@Log4j class LoggerLog4jWithImport {
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoggerLog4jWithImport.class);
  <clinit>() {
  }
  LoggerLog4jWithImport() {
    super();
  }
}
@Log4j(topic = "DifferentName") class LoggerLog4jWithDifferentName {
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("DifferentName");
  <clinit>() {
  }
  LoggerLog4jWithDifferentName() {
    super();
  }
}
@Log4j(topic = LoggerLog4jWithStaticField.TOPIC) class LoggerLog4jWithStaticField {
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoggerLog4jWithStaticField.TOPIC);
  static final String TOPIC = "StaticField";
  <clinit>() {
  }
  LoggerLog4jWithStaticField() {
    super();
  }
}
@Log4j enum LoggerLog4jWithEnum {
  CONSTANT(),
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoggerLog4jWithEnum.class);
  <clinit>() {
  }
  LoggerLog4jWithEnum() {
    super();
  }
}
class LoggerLog4jWithInnerEnum {
  @Log4j enum Inner {
    CONSTANT(),
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Inner.class);
    <clinit>() {
    }
    Inner() {
      super();
    }
  }
  LoggerLog4jWithInnerEnum() {
    super();
  }
}