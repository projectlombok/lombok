import lombok.extern.slf4j.Slf4j;
@Slf4j(topic = 42) class LoggerSlf4jWithIntegerTopic {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(42);
  <clinit>() {
  }
  LoggerSlf4jWithIntegerTopic() {
    super();
  }
}