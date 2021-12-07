import lombok.Synchronized;

public class SynchronizedInAnonymousClass {
	Object annonymous = new Object() {
		class Inner {
			@Synchronized
			public void foo() {
				String foo = "bar";
			}
		}
	};
}