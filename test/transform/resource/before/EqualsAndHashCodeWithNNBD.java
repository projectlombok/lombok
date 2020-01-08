// version 8:
import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
class EqualsAndHashCodeWithNNBD {
	@lombok.EqualsAndHashCode @org.eclipse.jdt.annotation.NonNullByDefault
	static class Inner {
	}
}
