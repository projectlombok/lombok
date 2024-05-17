class LoggerXSlf4j {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger(LoggerXSlf4j.class);
}
class LoggerXSlf4jWithImport {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger(LoggerXSlf4jWithImport.class);
}
class LoggerXSlf4jWithDifferentName {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger("DifferentName");
}
class LoggerXSlf4jWithStaticField {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger(LoggerXSlf4jWithStaticField.TOPIC);
	static final String TOPIC = "StaticField";
}
