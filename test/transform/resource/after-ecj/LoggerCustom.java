@lombok.CustomLog class LoggerCustomLog {
  private static final MyLogger log = MyLoggerFactory.create(LoggerCustomLog.class);
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
  static MyLogger create(Class<?> clazz) {
    return null;
  }
}
class MyLogger {
  MyLogger() {
    super();
  }
}