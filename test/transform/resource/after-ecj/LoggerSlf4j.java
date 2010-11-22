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