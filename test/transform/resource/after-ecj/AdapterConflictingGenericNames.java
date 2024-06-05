//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
import java.util.List;

public class AdapterConflictingGenericNames<T> {

	public static class AdapterClass implements FooInterface {
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public <T extends java.lang.Object> java.util.List<T> getListOfType(final java.lang.Class<T> clazz) {
			return java.util.Collections.emptyList();
		}
	}


	public static interface FooInterface {
		<T> List<T> getListOfType(Class<T> clazz);
	}
}
