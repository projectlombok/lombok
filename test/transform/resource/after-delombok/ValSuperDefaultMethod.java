// version 8:9
class ValSuperDefaultMethod implements Default {
	public void test() {
		final java.lang.String a = "";
		Default.super.method();
	}
}

interface Default {
	default void method() {
	}
}