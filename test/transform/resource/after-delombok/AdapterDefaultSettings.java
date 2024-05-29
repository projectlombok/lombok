//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
public class AdapterDefaultSettings {

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
		public int countMyObjects(final java.lang.String userName) throws java.lang.IllegalArgumentException {
			throw new java.lang.UnsupportedOperationException();
		}
	}

	public static interface FooInterface {
		void doStuff();
	
		int countMyObjects(String userName) throws IllegalArgumentException;
	
		String getStatus() throws IllegalStateException;
	}
}