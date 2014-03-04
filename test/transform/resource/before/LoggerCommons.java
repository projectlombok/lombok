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
