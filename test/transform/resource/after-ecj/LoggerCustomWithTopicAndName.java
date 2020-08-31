@lombok.CustomLog(topic = "t") class LoggerCustomLog {
  private static final MyLoggerFactory log = MyLoggerFactory.create(LoggerCustomLog.class.getName(), "t", null, LoggerCustomLog.class, "t");
  <clinit>() {
  }
  LoggerCustomLog() {
    super();
  }
}
@lombok.CustomLog(topic = LoggerCustomLogWithStaticField.TOPIC) class LoggerCustomLogWithStaticField {
  private static final MyLoggerFactory log = MyLoggerFactory.create(LoggerCustomLogWithStaticField.class.getName(), LoggerCustomLogWithStaticField.TOPIC, null, LoggerCustomLogWithStaticField.class, LoggerCustomLogWithStaticField.TOPIC);
  static final String TOPIC = "StaticField";
  <clinit>() {
  }
  LoggerCustomLogWithStaticField() {
    super();
  }
}
class MyLoggerFactory {
  MyLoggerFactory() {
    super();
  }
  static MyLoggerFactory create(String name, String t1, Object o, Class<?> clazz, String t2) {
    return null;
  }
}
