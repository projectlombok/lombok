import lombok.experimental.FieldNameConstants;

public class FieldNameConstantsInAnonymousClass {
	Object annonymous = new Object() {
		@FieldNameConstants
		class Inner {
			private String string;
		}
	};
}