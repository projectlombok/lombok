import lombok.extern.java.Log;

@lombok.extern.java.Log
class LoggerJul {
}

@Log
class LoggerJulWithImport {
}

@Log(topic="DifferentName")
class LoggerJulWithDifferentName {
}

@Log(topic=LoggerJulWithStaticField.TOPIC)
class LoggerJulWithStaticField {
	static final String TOPIC = "StaticField";
}

@Log
enum LoggerJulWithEnum {
	CONSTANT;
}

class LoggerJulWithInnerEnum {
	@Log
	enum Inner {
		CONSTANT;
	}
}