//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
//version 14:
import javax.annotation.Nullable;
import javax.annotation.Tainted;

record DelegateOnRecord(SomeInterface runnable) {
	interface SomeInterface {
		@Nullable
		@Tainted
		String getString(@Nullable @Tainted String input);
	}

	@javax.annotation.Nullable
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String getString(@javax.annotation.Nullable final java.lang.String input) {
		return this.runnable.getString(input);
	}
}
