//version 8:
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
class LoggerLog4j2WithStaticField {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LoggerLog4j2WithStaticField.TOPIC);
	static final String TOPIC = "StaticField";
}
enum LoggerLog4j2WithEnum {
	CONSTANT;
	@java.lang.SuppressWarnings("all")
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LoggerLog4j2WithEnum.class);
}
class LoggerLog4j2WithInnerEnum {

	enum Inner {
		CONSTANT;
		@java.lang.SuppressWarnings("all")
		private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(Inner.class);
	}
}