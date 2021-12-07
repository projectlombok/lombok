import lombok.Getter;

public class GetterInAnonymousClass {
	Object annonymous = new Object() {
		@Getter
		class Inner {
			private String string;
		}
	};
}