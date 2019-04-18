import java.util.List;

import lombok.Builder;
import java.util.*;

class BuilderGenericMethod<T> {
	@Builder
	public <N extends Number> Map<N,T> foo(int a, long b) {
		return null;
	}
}
