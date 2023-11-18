// version 14:
import lombok.extern.slf4j.Slf4j;
public @Slf4j record LoggerConfigOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
}
