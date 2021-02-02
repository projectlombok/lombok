// version 14:
import lombok.val;
public class ValSwitchExpression {
  public ValSwitchExpression() {
    super();
  }
  public void method(int arg) {
    final @val var x =     switch (arg) {
    default ->
        {
          final @val var s = "string";
          yield arg;
        }
    };
  }
}