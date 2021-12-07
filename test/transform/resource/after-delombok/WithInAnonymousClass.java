public class WithInAnonymousClass {
	Object annonymous = new Object() {

		class Inner {
			private Inner(String string) {
			}

			private String string;

			/**
			 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
			 */
			@java.lang.SuppressWarnings("all")
			public Inner withString(final String string) {
				return this.string == string ? this : new Inner(string);
			}
		}
	};
}
