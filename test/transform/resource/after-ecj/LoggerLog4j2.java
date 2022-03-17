import lombok.extern.log4j.Log4j2;
@lombok.extern.log4j.Log4j2 class LoggerLog4j2 {
  private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LoggerLog4j2.class);
  <clinit>() {
  }
  LoggerLog4j2() {
    super();
  }
}
@Log4j2 class LoggerLog4j2WithImport {
  private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LoggerLog4j2WithImport.class);
  <clinit>() {
  }
  LoggerLog4j2WithImport() {
    super();
  }
}
@Log4j2(topic = "DifferentName") class LoggerLog4j2WithDifferentName {
  private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger("DifferentName");
  <clinit>() {
  }
  LoggerLog4j2WithDifferentName() {
    super();
  }
}
@Log4j2(topic = LoggerLog4j2WithStaticField.TOPIC) class LoggerLog4j2WithStaticField {
  private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LoggerLog4j2WithStaticField.TOPIC);
  static final String TOPIC = "StaticField";
  <clinit>() {
  }
  LoggerLog4j2WithStaticField() {
    super();
  }
}
@Log4j2 enum LoggerLog4j2WithEnum {
  CONSTANT(),
  private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LoggerLog4j2WithEnum.class);
  <clinit>() {
  }
  LoggerLog4j2WithEnum() {
    super();
  }
}
class LoggerLog4j2WithInnerEnum {
  @Log4j2 enum Inner {
    CONSTANT(),
    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(Inner.class);
    <clinit>() {
    }
    Inner() {
      super();
    }
  }
  LoggerLog4j2WithInnerEnum() {
    super();
  }
}