//CONF: lombok.log.custom.declaration = MyLogger MyLoggerFactory.create(TYPE)
import lombok.AccessLevel;
import lombok.CustomLog;

@CustomLog(access = AccessLevel.PUBLIC)
class LoggerCustomAccessPublic {
}

@CustomLog(access = AccessLevel.MODULE)
class LoggerCustomAccessModule {
}

@CustomLog(access = AccessLevel.PROTECTED)
class LoggerCustomAccessProtected {
}

@CustomLog(access = AccessLevel.PACKAGE)
class LoggerCustomAccessPackage {
}

@CustomLog(access = AccessLevel.PRIVATE)
class LoggerCustomAccessPrivate {
}

@CustomLog(access = AccessLevel.NONE)
class LoggerCustomAccessNone {
}
class MyLoggerFactory {
	static MyLogger create(Class<?> clazz) {
		return null;
	}
}
class MyLogger {
}

