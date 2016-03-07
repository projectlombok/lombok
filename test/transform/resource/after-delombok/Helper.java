class HelperTest {
	{
		final int z = 5;
		if (Boolean.TRUE) {
			class H1 {
				void foo(int x) {
					System.out.println("Hello, " + (x + z));
				}
			}
			final H1 $H1 = new H1();
			$H1.foo(10);
			class H2 {
				void bar() {
					$H1.foo(12);
				}
			}
		}
	}
}
