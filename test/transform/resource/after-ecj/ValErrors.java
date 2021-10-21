import lombok.val;
public class ValErrors {
  public ValErrors() {
    super();
  }
  public void unresolvableExpression() {
    final @val java.lang.Object c = d;
  }
  public void arrayInitializer() {
    final @val java.lang.Object e = {"foo", "bar"};
  }
}