import lombok.extern.jbosslog.JBossLog;

@lombok.extern.jbosslog.JBossLog
class LoggerJBossLog {
}

@JBossLog
class LoggerJBossLogWithImport {
}

class LoggerJBossLogOuter {
	@lombok.extern.jbosslog.JBossLog
	static class Inner {
		
	}
}

@JBossLog
enum LoggerJBossLogWithEnum {
	CONSTANT;
}

class LoggerJBossLogWithInnerEnum {
	@JBossLog
	enum Inner {
		CONSTANT;
	}
}

@JBossLog(topic="DifferentLogger")
class LoggerJBossLogWithDifferentLoggerName {
}

@JBossLog(topic=LoggerJBossLogWithStaticField.TOPIC)
class LoggerJBossLogWithStaticField {
	static final String TOPIC = "StaticField";
}