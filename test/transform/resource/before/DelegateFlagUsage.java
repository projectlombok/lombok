//conf: lombok.delegate.flagUsage = warning
//skip compare content: We're just checking if the flagUsage key works.
public class DelegateFlagUsage {
	@lombok.experimental.Delegate Runnable r = null;
}