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
class LoggerLog4jWithStaticField {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoggerLog4jWithStaticField.TOPIC);
	static final String TOPIC = "StaticField";
}
