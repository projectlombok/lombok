public class VarModifier {
	private String field = "";
	public void testComplex() {
		final char[] shouldBeFinalCharArray = field.toCharArray();
		char[] shouldBeCharArray = field.toCharArray();
	}
}
