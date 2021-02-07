import java.util.Arrays;
import java.util.List;

class ExtensionMethodChain {
	public void test() {
		ExtensionMethodChain.Extensions.intValue("1").intValue();
	}


	static class Extensions {
		public static Integer intValue(String s) {
			return Integer.valueOf(s);
		}
	}
}