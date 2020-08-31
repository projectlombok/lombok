import lombok.extern.slf4j.XSlf4j;

@lombok.extern.slf4j.XSlf4j
class LoggerXSlf4j {
}

@XSlf4j
class LoggerXSlf4jWithImport {
}

@XSlf4j(topic="DifferentName")
class LoggerXSlf4jWithDifferentName {
}

@XSlf4j(topic=LoggerXSlf4jWithStaticField.TOPIC)
class LoggerXSlf4jWithStaticField {
	static final String TOPIC = "StaticField";
}