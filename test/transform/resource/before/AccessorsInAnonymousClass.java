import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class AccessorsInAnonymousClass {
	Object annonymous = new Object() {
		@Getter
		@Setter
		@Accessors(fluent = true)
		class Inner {
			private String string;
		}
	};
}