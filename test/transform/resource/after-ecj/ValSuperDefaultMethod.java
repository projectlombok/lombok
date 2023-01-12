import lombok.val;
class ValSuperDefaultMethod implements Default {
  ValSuperDefaultMethod() {
    super();
  }
  public void test() {
    final @val java.lang.String a = "";
    Default.super.method();
  }
}
interface Default {
  default void method() {
  }
}