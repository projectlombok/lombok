import lombok.experimental.Helper;

class HelperTest {
	void test() {
		@Helper class H1 {
			void foo() {
				System.out.println("Hello");
			}
		}
		
		foo();
		
		@Helper class H2 {
			void bar() {
				foo();
			}
		}
	}
}