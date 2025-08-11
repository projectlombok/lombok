class ExtensionMethodThis {
	public void test() {
		ExtensionMethodThis.Extensions.hello(this);
		hello();
	}
	
	private void hello() {
	}
	
	static class Extensions {
		public static void hello(ExtensionMethodThis self) {	
		}
	}
}
