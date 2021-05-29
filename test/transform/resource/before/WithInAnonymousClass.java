import lombok.With;

public class WithInAnonymousClass {
	Object annonymous = new Object() {
		@With
		class Inner {
			private Inner(String string) { }
			
			private String string;
		}
	};
}