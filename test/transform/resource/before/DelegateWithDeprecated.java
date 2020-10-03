//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
import lombok.experimental.Delegate;

class DelegateWithDeprecated {
	@Delegate private Bar bar;

	private interface Bar {
		@Deprecated
		void deprecatedAnnotation();
		/**
		 * @deprecated
		 */
		void deprecatedComment();
		void notDeprecated();
	}
}