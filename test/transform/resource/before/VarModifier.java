import lombok.experimental.var;

public class VarModifier {
	private String field = "";

	public void testComplex() {
		final var shouldBeFinalCharArray = field.toCharArray();
		var shouldBeCharArray = field.toCharArray();
	}
}