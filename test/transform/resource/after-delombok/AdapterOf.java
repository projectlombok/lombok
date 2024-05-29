import java.util.Optional;

public class AdapterOf {

	public static abstract class AdapterClass implements FooInterface, GooInterface, HooInterface {

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

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.util.Optional<java.lang.String> gooMethod() {
			throw new java.lang.UnsupportedOperationException();
		}
	}

	public static interface FooInterface {
		void doStuff();
	
		int countMyObjects(String userName) throws IllegalArgumentException;
	
		String getStatus() throws IllegalStateException;
	}

	public static interface GooInterface {
		Optional<String> gooMethod();
	}

	public static interface HooInterface {
		void wontImplement();
	}
}