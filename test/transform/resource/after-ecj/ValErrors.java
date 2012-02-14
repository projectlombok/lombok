import lombok.val;
public class ValErrors {
  public ValErrors() {
    super();
  }
  public void unresolvableExpression() {
    val c = d;
  }
  public void arrayInitializer() {
    val e = {"foo", "bar"};
  }
}