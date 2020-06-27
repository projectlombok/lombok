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
class LoggerJulWithStaticField {
	@java.lang.SuppressWarnings("all")
	private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerJulWithStaticField.TOPIC);
	static final String TOPIC = "StaticField";
}