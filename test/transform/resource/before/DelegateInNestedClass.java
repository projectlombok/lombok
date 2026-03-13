//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
import lombok.experimental.Delegate;

class DelegateInNestedClass {
	void localClass() {
		class LocalClass {
			@Delegate private final java.lang.Runnable field = null;
		}
	}
	
	void anonymousClass() {
		Runnable r = new Runnable() {
			@Delegate private final java.lang.Runnable field = null;
		};
	}
	
	class InnerClass {
		@Delegate private final java.lang.Runnable field = null;
	}
	
	static class StaticClass {
		@Delegate private final java.lang.Runnable field = null;
	}
}
