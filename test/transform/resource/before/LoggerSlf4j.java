import lombok.extern.slf4j.Slf4j;

@lombok.extern.slf4j.Slf4j
class LoggerSlf4j {
}

@Slf4j
class LoggerSlf4jWithImport {
}

@Slf4j
enum LoggerSlf4jWithEnum {
	CONSTANT;
}

class LoggerSlf4jWithInnerEnum {
	@Slf4j
	enum Inner {
		CONSTANT;
	}
}

class LoggerSlf4jOuter {
	@lombok.extern.slf4j.Slf4j
	static class Inner {
		
	}
}

@Slf4j(topic="DifferentLogger")
class LoggerSlf4jWithDifferentLoggerName {
}

@Slf4j(topic=LoggerSlf4jWithStaticField.TOPIC)
class LoggerSlf4jWithStaticField {
	static final String TOPIC = "StaticField";
}

@Slf4j(topic=LoggerSlf4jWithTwoStaticFields.TOPIC + LoggerSlf4jWithTwoStaticFields.TOPIC)
class LoggerSlf4jWithTwoStaticFields {
	static final String TOPIC = "StaticField";
}
