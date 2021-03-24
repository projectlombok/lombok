// version 14:
import lombok.extern.slf4j.Slf4j;
public @Slf4j record LoggerSlf4jOnRecord(org log, String a) {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jOnRecord.class);
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  <clinit>() {
  }
  public LoggerSlf4jOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
}
