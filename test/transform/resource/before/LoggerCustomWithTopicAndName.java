//CONF: lombok.log.custom.declaration = MyLoggerFactory.create(NAME,TOPIC,NULL,TYPE,TOPIC)
@lombok.CustomLog(topic="t")
class LoggerCustomLog {
}

@lombok.CustomLog(topic=LoggerCustomLogWithStaticField.TOPIC)
class LoggerCustomLogWithStaticField {
	static final String TOPIC = "StaticField";
}

class MyLoggerFactory {
	static MyLoggerFactory create(String name, String t1, Object o, Class<?> clazz, String t2) {
		return null;
	}
}
