//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
//conf: lombok.delegate.flagUsage = warning
//skip compare content: We're just checking if the flagUsage key works.
public class DelegateFlagUsage {
	@lombok.experimental.Delegate Runnable r = null;
}