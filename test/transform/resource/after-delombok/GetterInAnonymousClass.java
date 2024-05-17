public class GetterInAnonymousClass {
	Object annonymous = new Object() {
		class Inner {
			private String string;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public String getString() {
				return this.string;
			}
		}
	};
}
