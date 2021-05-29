import lombok.ToString;

public class ToStringInAnonymousClass {
	Object annonymous = new Object() {
		@ToString
		class Inner {
			private String string;
		}
	};
}