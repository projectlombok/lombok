public class ToStringInAnonymousClass {
	Object annonymous = new Object() {

		class Inner {
			private String string;

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "Inner(string=" + this.string + ")";
			}
		}
	};
}
