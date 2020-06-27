class LoggerCustomLog {
	@java.lang.SuppressWarnings("all")
	private static final MyLoggerFactory log = MyLoggerFactory.create(LoggerCustomLog.class.getName(), "t", null, LoggerCustomLog.class, "t");
}

class LoggerCustomLogWithStaticField {
	@java.lang.SuppressWarnings("all")
	private static final MyLoggerFactory log = MyLoggerFactory.create(LoggerCustomLogWithStaticField.class.getName(), LoggerCustomLogWithStaticField.TOPIC, null, LoggerCustomLogWithStaticField.class, LoggerCustomLogWithStaticField.TOPIC);
	static final String TOPIC = "StaticField";
}

class MyLoggerFactory {
	static MyLoggerFactory create(String name, String t1, Object o, Class<?> clazz, String t2) {
		return null;
	}
}