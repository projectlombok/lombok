import lombok.AccessLevel;
class LoggerCustomAccessPublic {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static final MyLogger log = MyLoggerFactory.create(LoggerCustomAccessPublic.class);

}
class LoggerCustomAccessModule {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	static final MyLogger log = MyLoggerFactory.create(LoggerCustomAccessModule.class);

}
class LoggerCustomAccessProtected {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected static final MyLogger log = MyLoggerFactory.create(LoggerCustomAccessProtected.class);

}
class LoggerCustomAccessPackage {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	static final MyLogger log = MyLoggerFactory.create(LoggerCustomAccessPackage.class);

}
class LoggerCustomAccessPrivate {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final MyLogger log = MyLoggerFactory.create(LoggerCustomAccessPrivate.class);

}
class LoggerCustomAccessNone {
}
class MyLoggerFactory {
	static MyLogger create(Class<?> clazz) {
		return null;
	}
}
class MyLogger {
}

