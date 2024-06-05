public class AdapterDontGenerateDefaultMethod {

	public static class AdapterClass implements FooInterface {

		@Override
		public String getStatus() {
			return "TestStatus";
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public void doStuff() {
			throw new java.lang.UnsupportedOperationException();
		}
	}

	public static interface FooInterface {
		void doStuff();
	
		default int countMyObjects(String userName) throws IllegalArgumentException {
			return 1;
		}
	
		String getStatus() throws IllegalStateException;
	}
}