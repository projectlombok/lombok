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
@Log(topic = LoggerJulWithStaticField.TOPIC) class LoggerJulWithStaticField {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerJulWithStaticField.TOPIC);
  static final String TOPIC = "StaticField";
  <clinit>() {
  }
  LoggerJulWithStaticField() {
    super();
  }
}
@Log enum LoggerJulWithEnum {
  CONSTANT(),
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerJulWithEnum.class.getName());
  <clinit>() {
  }
  LoggerJulWithEnum() {
    super();
  }
}
class LoggerJulWithInnerEnum {
  @Log enum Inner {
    CONSTANT(),
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Inner.class.getName());
    <clinit>() {
    }
    Inner() {
      super();
    }
  }
  LoggerJulWithInnerEnum() {
    super();
  }
}