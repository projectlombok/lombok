class LoggerLog4j {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoggerLog4j.class);
}
class LoggerLog4jWithImport {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoggerLog4jWithImport.class);
}
class LoggerLog4jWithDifferentName {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("DifferentName");
}
class LoggerLog4jWithStatic {
	@java.lang.SuppressWarnings("all")
	private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
}
