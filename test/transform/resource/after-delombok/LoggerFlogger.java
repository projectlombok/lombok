class LoggerFlogger {
	@java.lang.SuppressWarnings("all")
	private static final com.google.common.flogger.FluentLogger log = com.google.common.flogger.FluentLogger.forEnclosingClass();
}
class LoggerFloggerWithImport {
	@java.lang.SuppressWarnings("all")
	private static final com.google.common.flogger.FluentLogger log = com.google.common.flogger.FluentLogger.forEnclosingClass();
}
class LoggerFloggerOuter {
	static class Inner {
		@java.lang.SuppressWarnings("all")
		private static final com.google.common.flogger.FluentLogger log = com.google.common.flogger.FluentLogger.forEnclosingClass();
	}
}
