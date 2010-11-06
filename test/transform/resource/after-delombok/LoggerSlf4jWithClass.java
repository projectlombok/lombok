class LoggerSlf4jWithClass {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(String.class);
}
class LoggerSlf4jWithClassList {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.util.List.class);
}
class LoggerSlf4jWithClassValue {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.lang.String.class);
}
class LoggerSlf4jWithClassVoid {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jWithClassVoid.class);
}
