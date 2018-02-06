import lombok.var;
public class VarNullInit {
  public VarNullInit() {
    super();
  }
  void method() {
    @var java.lang.Object x = null;
  }
}