class LoggerLog4j2 {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LoggerLog4j2.class);
}
class LoggerLog4j2WithImport {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LoggerLog4j2WithImport.class);
}
class LoggerLog4j2WithDifferentName {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger("DifferentName");
}
class LoggerLog4j2WithStatic {
	@java.lang.SuppressWarnings("all")
	private final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(this.getClass());
}
