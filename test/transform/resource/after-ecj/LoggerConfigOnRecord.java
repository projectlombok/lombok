import lombok.extern.slf4j.Slf4j;
@Slf4j record LoggerConfigOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public LoggerConfigOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
}
