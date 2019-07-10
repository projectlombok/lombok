//CONF: lombok.log.custom.declaration = before.MyLogger before.MyLoggerFactory.create(TYPE)
package before;
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