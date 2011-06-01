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