import lombok.val;
public class ValUndenotable {
  public ValUndenotable() {
    super();
  }
  public void method(int arg) {
    final @val var x = new Object() {
      x() {
        super();
      }
      void foo() {
      }
    };
    x.foo();
  }
}