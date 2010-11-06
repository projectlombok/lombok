class LoggerLog4j {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoggerLog4j.class);
}
class LoggerLog4jString {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(String.class);
}
class LoggerLog4jJavaLangString {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(java.lang.String.class);
}