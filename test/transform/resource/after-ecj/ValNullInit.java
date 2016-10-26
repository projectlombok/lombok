import lombok.val;

class ValNullInit {
  ValNullInit() {
    super();
  }
  void method() {
    final @val java.lang.Object x = null;
  }
}