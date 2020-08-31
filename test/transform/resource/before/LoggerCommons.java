import lombok.extern.apachecommons.CommonsLog;

@lombok.extern.apachecommons.CommonsLog
class LoggerCommons {
}

@CommonsLog
class LoggerCommonsWithImport {
}

@CommonsLog(topic="DifferentName")
class LoggerCommonsWithDifferentName {
}

@CommonsLog(topic=LoggerCommonsWithStaticField.TOPIC)
class LoggerCommonsWithStaticField {
	static final String TOPIC = "StaticField";
}