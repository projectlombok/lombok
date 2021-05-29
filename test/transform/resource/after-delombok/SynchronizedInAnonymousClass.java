public class SynchronizedInAnonymousClass {
	Object annonymous = new Object() {

		class Inner {
			@java.lang.SuppressWarnings("all")
			private final java.lang.Object $lock = new java.lang.Object[0];

			public void foo() {
				synchronized (this.$lock) {
					String foo = "bar";
				}
			}
		}
	};
}
