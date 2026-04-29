// version 14:
public class ValUndenotable {
	public void method(int arg) {
		final var x = new Object() {
			void foo() {
			}
		};
		x.foo();
	}
}