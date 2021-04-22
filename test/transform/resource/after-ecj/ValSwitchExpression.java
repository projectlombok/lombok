// version 14:
import lombok.val;
public class ValSwitchExpression {
  public ValSwitchExpression() {
    super();
  }
  public void method(int arg) {
    final @val int x =     switch (arg) {
    default ->
        {
          final @val java.lang.String s = "string";
          yield arg;
        }
    };
  }
}