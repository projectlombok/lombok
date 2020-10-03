//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
import lombok.AccessLevel;
import lombok.experimental.Delegate;
import lombok.Getter;

@Getter
class DelegateOnGetterNone {

	@Delegate @Getter(AccessLevel.NONE) private final Bar bar = null;

	private interface Bar {
		void setList(java.util.ArrayList<java.lang.String> list);
		int getInt();
	}
}
