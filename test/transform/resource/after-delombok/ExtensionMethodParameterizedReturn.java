import java.util.Collection;
import java.util.List;
import java.util.function.Function;

class ExtensionMethodParameterizedReturn {
	void test() {
		List<String> values = null;
		process(ExtensionMethodParameterizedReturn.Extensions.mapAllToList(values, s -> s + ";"));
	}

	void process(List<String> values) {
	}

	static class Extensions {
		public static <V, R> List<R> mapAllToList(Collection<? extends V> values, Function<V, R> mapper) {
			return null;
		}
	}
}
