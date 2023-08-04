import lombok.experimental.Helper;

public class HelperInInitializationBlock {
	{
		final int z = 5;
		if (Boolean.TRUE) {
			@Helper class H1 {
				void foo(int x) {
					System.out.println("Hello, " + (x + z));
				}
			}
			
			foo(10);
			
			@Helper class H2 {
				void bar() {
					foo(12);
				}
			}
		}
	}
}