//version 8:
import lombok.NonNull;

public class ConstructorsInAnonymousClass {
	Object annonymous = new Object() {

		class Inner {
			private String string;
			@NonNull
			private String string2;

			@java.lang.SuppressWarnings("all")
			public Inner(final String string, @NonNull final String string2) {
				if (string2 == null) {
					throw new java.lang.NullPointerException("string2 is marked non-null but is null");
				}
				this.string = string;
				this.string2 = string2;
			}

			@java.lang.SuppressWarnings("all")
			public Inner(@NonNull final String string2) {
				if (string2 == null) {
					throw new java.lang.NullPointerException("string2 is marked non-null but is null");
				}
				this.string2 = string2;
			}

			@java.lang.SuppressWarnings("all")
			public Inner() {
			}
		}
	};
}
