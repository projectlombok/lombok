class LoggerJul {
	@java.lang.SuppressWarnings("all")
	private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerJul.class.getName());
}
class LoggerJulWithImport {
	@java.lang.SuppressWarnings("all")
	private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerJulWithImport.class.getName());
}
class LoggerJulWithDifferentName {
	@java.lang.SuppressWarnings("all")
	private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("DifferentName");
}
class LoggerJulWithStatic {
	@java.lang.SuppressWarnings("all")
	private final java.util.logging.Logger log = java.util.logging.Logger.getLogger(this.getClass().getName());
}
