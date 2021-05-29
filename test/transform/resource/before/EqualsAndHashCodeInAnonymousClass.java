import lombok.EqualsAndHashCode;

public class EqualsAndHashCodeInAnonymousClass {
	Object annonymous = new Object() {
		@EqualsAndHashCode
		class Inner {
			private String string;
		}
	};
}