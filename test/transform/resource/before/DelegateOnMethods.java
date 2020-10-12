//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
import lombok.experimental.Delegate;

abstract class DelegateOnMethods {

	@Delegate
	public abstract Bar getBar();

	public static interface Bar {
		void bar(java.util.ArrayList<java.lang.String> list);
	}
}