class HelperTest {
	void test() {
		class H1 {
			void foo() {
				System.out.println("Hello");
			}
		}
		final H1 $H1 = new H1();
		$H1.foo();
		class H2 {
			void bar() {
				$H1.foo();
			}
		}
	}
}
