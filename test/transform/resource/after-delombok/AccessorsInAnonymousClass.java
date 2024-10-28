public class AccessorsInAnonymousClass {
	Object annonymous = new Object() {
		class Inner {
			private String string;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public String string() {
				return this.string;
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public Inner string(final String string) {
				this.string = string;
				return this;
			}
		}
	};
}
