import lombok.Value;

public class ValueInAnonymousClass {
	Object annonymous = new Object() {
		@Value
		class Inner {
			private String string;
		}
	};
}