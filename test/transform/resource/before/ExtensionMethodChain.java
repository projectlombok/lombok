import java.util.Arrays;
import java.util.List;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodChain.Extensions.class)
class ExtensionMethodChain {
	
	public void test() {
		"1".intValue().intValue();
	}
	
	static class Extensions {
		public static Integer intValue(String s) {
			return Integer.valueOf(s);
		}
	}
}
