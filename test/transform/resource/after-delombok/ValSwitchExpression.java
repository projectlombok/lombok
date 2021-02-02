// version 14:
public class ValSwitchExpression {
	public void method(int arg) {
		final var x = switch (arg) {
			default -> {
				final var s = "string";
				yield arg;
			}
		};
	}
}