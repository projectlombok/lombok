//CONF: lombok.log.custom.declaration = MyLogger MyLoggerFactory.create(TYPE)
@lombok.CustomLog
class LoggerCustomLog {
}

class MyLoggerFactory {
	static MyLogger create(Class<?> clazz) {
		return null;
	}
}

class MyLogger {
}