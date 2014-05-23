//CONF: lombok.nonNull.exceptionType = 12345
//skip compare content: Just testing appropriate generation of error message.

@lombok.Data
public class NonNullWithMistypedException {
	private @lombok.NonNull String test;
}
