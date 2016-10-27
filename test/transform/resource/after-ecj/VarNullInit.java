import lombok.experimental.var;

class VarNullInit {
  VarNullInit() {
    super();
  }
  void method() {
    @var java.lang.Object x = null;
  }
}