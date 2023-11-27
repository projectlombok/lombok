// version 19:
import lombok.extern.flogger.Flogger;
class LoggerFloggerRecord {
  public @Flogger record Inner(String x) {
/* Implicit */    private final String x;
    private static final com.google.common.flogger.FluentLogger log = com.google.common.flogger.FluentLogger.forEnclosingClass();
    <clinit>() {
    }
  }
  LoggerFloggerRecord() {
    super();
  }
}