// version 16: the SuppressWarnings is not emitted in java14/15 to work around a javac bug.
public record LoggerSlf4jOnRecord(String a, String b) {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jOnRecord.class);
}
