class LoggerCustomLog {
	@java.lang.SuppressWarnings("all")
	private static final MyLogger log = MyLoggerFactory.create(LoggerCustomLog.class);
}

class MyLoggerFactory {
	static MyLogger create(Class<?> clazz) {
		return null;
	}
}

class MyLogger {
}