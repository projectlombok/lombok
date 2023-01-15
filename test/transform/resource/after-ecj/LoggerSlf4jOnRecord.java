// version 14:
import lombok.extern.slf4j.Slf4j;
public @Slf4j record LoggerSlf4jOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jOnRecord.class);
  <clinit>() {
  }
  public LoggerSlf4jOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
}
