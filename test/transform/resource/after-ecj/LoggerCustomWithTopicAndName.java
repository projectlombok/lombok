@lombok.CustomLog(topic = "t") class LoggerCustomLog {
  private static final MyLoggerFactory log = MyLoggerFactory.create(LoggerCustomLog.class.getName(), "t", null, LoggerCustomLog.class, "t");
  <clinit>() {
  }
  LoggerCustomLog() {
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
