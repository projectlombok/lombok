// version 14:
import lombok.val;

public class ValSwitchExpression {
	public void method(int arg) {
		val x = switch (arg) {
			default -> {
				val s = "string";
				yield arg;
			}
		};
	}
}
