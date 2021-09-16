import lombok.Getter;

public class GetterLazyInAnonymousClass {
	Object annonymous = new Object() {
		class Inner {
			@Getter(lazy = true)
			private final String string = "test";
		}
	};
}