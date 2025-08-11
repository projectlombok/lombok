import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodThis.Extensions.class)
class ExtensionMethodThis {
	public void test() {
		this.hello();
		hello();
	}
	
	private void hello() {
	}
	
	static class Extensions {
		public static void hello(ExtensionMethodThis self) {
		}
	}
}
