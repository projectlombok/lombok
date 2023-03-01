// version 8:9
import lombok.val;

class ValSuperDefaultMethod implements Default {
	public void test() {
		val a = "";
		Default.super.method();
	}
	
}

interface Default {
	default void method() {
		
	}
}