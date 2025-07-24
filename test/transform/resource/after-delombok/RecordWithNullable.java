//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
//version 14:
import javax.annotation.Nullable;

record DelegateOnRecord(SomeInterface runnable) {
	interface SomeInterface {
		@Nullable
		String getString();
	}

	@javax.annotation.Nullable
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String getString() {
		return this.runnable.getString();
	}
}
