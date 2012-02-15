import lombok.val;
public class ValInMultiDeclaration {
  public ValInMultiDeclaration() {
    super();
  }
  public void test() {
    final @val int x = 10;
    final @val java.lang.String y = "";
  }
}
