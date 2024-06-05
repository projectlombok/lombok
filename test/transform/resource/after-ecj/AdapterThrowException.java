public class AdapterThrowException {

	public static class AdapterClass implements FooInterface {

		@Override
		public String getStatus() {
			return "TestStatus";
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public void doStuff() {
			throw new java.lang.IllegalStateException("NOT_IMPLEMENTED");
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public int countMyObjects(final java.lang.String userName) throws java.lang.IllegalArgumentException {
			throw new java.lang.IllegalStateException("NOT_IMPLEMENTED");
		}
	}

	public static interface FooInterface {
		void doStuff();
	
		int countMyObjects(String userName) throws IllegalArgumentException;
	
		String getStatus() throws IllegalStateException;
	}
}