class LoggerCommons {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggerCommons.class);
}
class LoggerCommonsWithImport {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggerCommonsWithImport.class);
}
class LoggerCommonsWithDifferentName {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog("DifferentName");
}
class LoggerCommonsWithStaticField {
	@java.lang.SuppressWarnings("all")
	private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggerCommonsWithStaticField.TOPIC);
	static final String TOPIC = "StaticField";
}