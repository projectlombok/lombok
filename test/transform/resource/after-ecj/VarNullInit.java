import lombok.experimental.var;

class VarNullInit {
  ValNullInit() {
    super();
  }
  void method() {
    final @var java.lang.Object x = null;
  }
}