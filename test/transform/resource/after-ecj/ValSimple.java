import lombok.val;
public class ValSimple {
  private String field = "field";
  private short field2 = 5;
  public ValSimple() {
    super();
  }
  private String method() {
    return "method";
  }
  private double method2() {
    return 2.0;
  }
  private void testVal(String param) {
    final @val java.lang.String fieldV = field;
    final @val java.lang.String methodV = method();
    final @val java.lang.String paramV = param;
    final @val java.lang.String valOfVal = fieldV;
    final @val java.lang.String operatorV = (fieldV + valOfVal);
    final @val short fieldW = field2;
    final @val double methodW = method2();
    byte localVar = 3;
    final @val int operatorW = (fieldW + localVar);
  }
}