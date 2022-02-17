import lombok.extern.slf4j.Slf4j;
@lombok.extern.slf4j.Slf4j class LoggerSlf4j {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4j.class);
  <clinit>() {
  }
  LoggerSlf4j() {
    super();
  }
}
@Slf4j class LoggerSlf4jWithImport {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jWithImport.class);
  <clinit>() {
  }
  LoggerSlf4jWithImport() {
    super();
  }
}
@Slf4j enum LoggerSlf4jWithEnum {
  CONSTANT(),
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jWithEnum.class);
  <clinit>() {
  }
  LoggerSlf4jWithEnum() {
    super();
  }
}
class LoggerSlf4jWithInnerEnum {
  @Slf4j enum Inner {
    CONSTANT(),
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Inner.class);
    <clinit>() {
    }
    Inner() {
      super();
    }
  }
  LoggerSlf4jWithInnerEnum() {
    super();
  }
}
class LoggerSlf4jOuter {
  static @lombok.extern.slf4j.Slf4j class Inner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Inner.class);
    <clinit>() {
    }
    Inner() {
      super();
    }
  }
  LoggerSlf4jOuter() {
    super();
  }
}

@Slf4j(topic = "DifferentLogger") class LoggerSlf4jWithDifferentLoggerName {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("DifferentLogger");
  <clinit>() {
  }
  LoggerSlf4jWithDifferentLoggerName() {
    super();
  }
}

@Slf4j(topic = LoggerSlf4jWithStaticField.TOPIC) class LoggerSlf4jWithStaticField {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jWithStaticField.TOPIC);
  static final String TOPIC = "StaticField";
  <clinit>() {
  }
  LoggerSlf4jWithStaticField() {
    super();
  }
}
@Slf4j(topic = (LoggerSlf4jWithTwoStaticFields.TOPIC + LoggerSlf4jWithTwoStaticFields.TOPIC)) class LoggerSlf4jWithTwoStaticFields {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger((LoggerSlf4jWithTwoStaticFields.TOPIC + LoggerSlf4jWithTwoStaticFields.TOPIC));
  static final String TOPIC = "StaticField";
  <clinit>() {
  }
  LoggerSlf4jWithTwoStaticFields() {
    super();
  }
}
