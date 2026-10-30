//version :9
import java.util.function.Function;
import lombok.experimental.ExtensionMethod;
import lombok.var;

@ExtensionMethod(value = ExtensionMethodVarInLambda.Extensions.class)
public class ExtensionMethodVarInLambda {
	public void test() {
		String test = "test";
		test.map(s -> {
			var reversed = s.reverse();
			return reversed;
		});
	}
	
	static class Extensions {
		public static <T, R> R map(T value, Function<T, R> mapper) {
			return mapper.apply(value);
		}
		
		public static String reverse(String string) {
			return new StringBuilder(string).reverse().toString();
		}
	}
}
