import lombok.extern.log4j.Log4j;

@lombok.extern.log4j.Log4j
class LoggerLog4j {
}

@Log4j
class LoggerLog4jWithImport {
}

@Log4j(topic="DifferentName")
class LoggerLog4jWithDifferentName {
}

@Log4j(topic=LoggerLog4jWithStaticField.TOPIC)
class LoggerLog4jWithStaticField {
	static final String TOPIC = "StaticField";
}