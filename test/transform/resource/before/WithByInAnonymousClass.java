//version 8:
import lombok.experimental.WithBy;

public class WithByInAnonymousClass {
	Object annonymous = new Object() {
		@WithBy
		class Inner {
			private Inner(String string) { }
			
			private String string;
		}
	};
}