// version 14:
public class ValSwitchExpression {
	public void method(int arg) {
		final int x = switch (arg) {
			default -> {
				final java.lang.String s = "string";
				yield arg;
			}
		};
	}
}