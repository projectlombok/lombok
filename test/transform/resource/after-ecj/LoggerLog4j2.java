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