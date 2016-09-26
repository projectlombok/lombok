import lombok.val;
public class ValFinal {
  public ValFinal() {
    super();
  }
  public void test() {
    final @val int x = 10;
  }
}
