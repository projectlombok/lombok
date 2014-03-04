import lombok.extern.java.Log;
@lombok.extern.java.Log class LoggerJul {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerJul.class.getName());
  <clinit>() {
  }
  LoggerJul() {
    super();
  }
}
@Log class LoggerJulWithImport {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerJulWithImport.class.getName());
  <clinit>() {
  }
  LoggerJulWithImport() {
    super();
  }
}
@Log(topic = "DifferentName") class LoggerJulWithDifferentName {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("DifferentName");
  <clinit>() {
  }
  LoggerJulWithDifferentName() {
    super();
  }
}