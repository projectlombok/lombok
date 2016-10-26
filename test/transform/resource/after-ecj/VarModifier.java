import lombok.experimental.var;
public class VarModifier {
  private String field = "";
  public VarModifier() {
    super();
  }
  public void testComplex() {
    final @var char[] shouldBeFinalCharArray = field.toCharArray();
    @var char[] shouldBeCharArray = field.toCharArray();

  }
}