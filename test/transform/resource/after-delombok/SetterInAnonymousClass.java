public class SetterInAnonymousClass {
	Object annonymous = new Object() {
		class Inner {
			private String string;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public void setString(final String string) {
				this.string = string;
			}
		}
	};
}
