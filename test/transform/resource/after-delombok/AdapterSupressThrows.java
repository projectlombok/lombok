public class AdapterSupressThrows {

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

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public int countMyObjects(final java.lang.String userName) {
			throw new java.lang.UnsupportedOperationException();
		}
	}


	public static interface FooInterface {
		void doStuff();

		int countMyObjects(String userName) throws IllegalArgumentException;

		String getStatus() throws IllegalStateException;
	}
}
