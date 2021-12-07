public class SuperBuilderInAnonymousClass {
	Object annonymous = new Object() {

		class InnerParent {
			private String string;
		}

		class InnerChild {
			private String string;
		}
	};
}
