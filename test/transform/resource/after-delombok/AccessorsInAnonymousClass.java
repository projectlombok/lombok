public class AccessorsInAnonymousClass {
	Object annonymous = new Object() {

		class Inner {
			private String string;

			@java.lang.SuppressWarnings("all")
			public String string() {
				return this.string;
			}

			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			public Inner string(final String string) {
				this.string = string;
				return this;
			}
		}
	};
}
