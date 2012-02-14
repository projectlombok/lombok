import lombok.val;
public class ValLessSimple {
  private short field2 = 5;
  {
    System.out.println("Hello");
    final @val int z = 20;
    final @val int x = 10;
    final @val int a = z;
    final @val short y = field2;
  }
  private String field = "field";
  public ValLessSimple() {
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
    final @val int a = 10;
    final @val int b = 20;
    {
      final @val java.lang.String methodV = method();
      final @val java.lang.String foo = (fieldV + methodV);
    }
  }
  private void testValInCatchBlock() {
    try 
      {
        final @val int x = (1 / 0);
      }
    catch (ArithmeticException e)       {
        final @val int y = 0;
      }
  }
}
