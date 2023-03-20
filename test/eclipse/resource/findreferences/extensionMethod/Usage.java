package pkg;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extension.class)
public class Usage {
	public void test() {
		private String string;
		string.test();
		string.test("a");
		string.test(1);
	}
}