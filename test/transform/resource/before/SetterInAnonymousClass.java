import lombok.Setter;

public class SetterInAnonymousClass {
	Object annonymous = new Object() {
		@Setter
		class Inner {
			private String string;
		}
	};
}