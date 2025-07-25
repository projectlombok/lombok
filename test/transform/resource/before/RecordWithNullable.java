//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
//version 14:
import lombok.experimental.Delegate;
import javax.annotation.Nullable;

record DelegateOnRecord(@Delegate SomeInterface runnable) {
	interface SomeInterface {
		@Nullable 
		String getString(@Nullable String input);
	}
}
