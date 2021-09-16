import lombok.extern.slf4j.Slf4j;

public class LoggerSlf4jInAnonymousClass {
	Object annonymous = new Object() {
		@Slf4j
		class Inner {
		}
	};
}