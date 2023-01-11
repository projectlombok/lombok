// version 14:
import lombok.extern.flogger.Flogger;
class LoggerFloggerRecord {
  public static @Flogger record Inner(com log) {
    private static final com.google.common.flogger.FluentLogger log = com.google.common.flogger.FluentLogger.forEnclosingClass();
/* Implicit */    private final String x;
    <clinit>() {
    }
    public Inner(String x) {
      super();
      .x = x;
    }
  }
  LoggerFloggerRecord() {
    super();
  }
}