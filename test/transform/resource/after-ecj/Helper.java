import lombok.experimental.Helper;
class HelperTest {
  HelperTest() {
    super();
  }
  void test() {
    @Helper class H1 {
      H1() {
        super();
      }
      void foo() {
        System.out.println("Hello");
      }
    }
    final H1 $H1 = new H1();
    $H1.foo();
    @Helper class H2 {
      H2() {
        super();
      }
      void bar() {
        $H1.foo();
      }
    }
  }
}
