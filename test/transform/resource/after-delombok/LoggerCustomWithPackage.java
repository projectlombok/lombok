package before;
class LoggerCustomLog {
	@java.lang.SuppressWarnings("all")
	private static final before.MyLogger log = before.MyLoggerFactory.create(LoggerCustomLog.class);
}

class MyLoggerFactory {
	static MyLogger create(Class<?> clazz) {
		return null;
	}
}

class MyLogger {
}