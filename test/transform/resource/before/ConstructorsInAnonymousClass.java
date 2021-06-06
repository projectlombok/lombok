//version 8:
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class ConstructorsInAnonymousClass {
	Object annonymous = new Object() {
		@AllArgsConstructor
		@RequiredArgsConstructor
		@NoArgsConstructor
		class Inner {
			private String string;
			@NonNull
			private String string2;
		}
	};
}