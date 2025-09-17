import lombok.AccessLevel;
import lombok.CustomLog;
@CustomLog(access = AccessLevel.PUBLIC) class LoggerCustomAccessPublic {
  public static final @java.lang.SuppressWarnings("all") @lombok.Generated MyLogger log = MyLoggerFactory.create(LoggerCustomAccessPublic.class);
  <clinit>() {
  }

  LoggerCustomAccessPublic() {
    super();
  }
}
@CustomLog(access = AccessLevel.MODULE) class LoggerCustomAccessModule {
  static final @java.lang.SuppressWarnings("all") @lombok.Generated MyLogger log = MyLoggerFactory.create(LoggerCustomAccessModule.class);
  <clinit>() {
  }

  LoggerCustomAccessModule() {
    super();
  }
}
@CustomLog(access = AccessLevel.PROTECTED) class LoggerCustomAccessProtected {
  protected static final @java.lang.SuppressWarnings("all") @lombok.Generated MyLogger log = MyLoggerFactory.create(LoggerCustomAccessProtected.class);
  <clinit>() {
  }

  LoggerCustomAccessProtected() {
    super();
  }
}
@CustomLog(access = AccessLevel.PACKAGE) class LoggerCustomAccessPackage {
  static final @java.lang.SuppressWarnings("all") @lombok.Generated MyLogger log = MyLoggerFactory.create(LoggerCustomAccessPackage.class);
  <clinit>() {
  }

  LoggerCustomAccessPackage() {
    super();
  }
}
@CustomLog(access = AccessLevel.PRIVATE) class LoggerCustomAccessPrivate {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated MyLogger log = MyLoggerFactory.create(LoggerCustomAccessPrivate.class);
  <clinit>() {
  }

  LoggerCustomAccessPrivate() {
    super();
  }
}
@CustomLog(access = AccessLevel.NONE) class LoggerCustomAccessNone {
  LoggerCustomAccessNone() {
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

