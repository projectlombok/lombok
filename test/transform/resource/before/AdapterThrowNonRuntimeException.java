//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
import lombok.experimental.Adapter;

public class AdapterThrowNonRuntimeException {

	@Adapter(throwException = java.lang.NoSuchMethodException.class, message = "NOT_IMPLEMENTED")
	public static class AdapterClass implements FooInterface {

		@Override
		public String getStatus() {
			return "TestStatus";
		}
	}

	public static interface FooInterface {
		void doStuff();
	
		int countMyObjects(String userName) throws IllegalArgumentException;
	
		String getStatus() throws IllegalStateException;
	}
}