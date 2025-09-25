//version 8: Our Log4j2 version doesn't support Java 6
import lombok.extern.log4j.Log4j2;

@lombok.extern.log4j.Log4j2
class LoggerLog4j2 {
}

@Log4j2
class LoggerLog4j2WithImport {
}

@Log4j2(topic="DifferentName")
class LoggerLog4j2WithDifferentName {
}

@Log4j2(topic=LoggerLog4j2WithStaticField.TOPIC)
class LoggerLog4j2WithStaticField {
	static final String TOPIC = "StaticField";
}

@Log4j2
enum LoggerLog4j2WithEnum {
	CONSTANT;
}

class LoggerLog4j2WithInnerEnum {
	@Log4j2
	enum Inner {
		CONSTANT;
	}
}