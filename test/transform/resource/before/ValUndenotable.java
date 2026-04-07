// version 14:
import lombok.val;

public class ValUndenotable {
	public void method(int arg) {
		val x = new Object() {
			void foo() {
			}
		};
		x.foo();
	}
}
