public class GetterInAnonymousClass {
	Object annonymous = new Object() {

		class Inner {
			private String string;

			@java.lang.SuppressWarnings("all")
			public String getString() {
				return this.string;
			}
		}
	};
}
