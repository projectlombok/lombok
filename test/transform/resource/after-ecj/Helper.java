import lombok.experimental.Helper;
class HelperTest {
  {
    final int z = 5;
    if (Boolean.TRUE)
        {
          @Helper class H1 {
            H1() {
              super();
            }
            void foo(int x) {
              System.out.println(("Hello, " + (x + z)));
            }
          }
          final H1 $H1 = new H1();
          $H1.foo(10);
          @Helper class H2 {
            H2() {
              super();
            }
            void bar() {
              $H1.foo(12);
            }
          }
        }
  }
  HelperTest() {
    super();
  }
}
