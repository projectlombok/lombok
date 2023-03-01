// version 16:
class LoggerFloggerRecord {
	public record Inner(String x) {
		@java.lang.SuppressWarnings("all")
		private static final com.google.common.flogger.FluentLogger log = com.google.common.flogger.FluentLogger.forEnclosingClass();
	}
}