import lombok.Builder;

public class BuilderInAnonymousClass {
	Object annonymous = new Object() {
		@Builder
		class Inner {
			private String string;
		}
	};
}