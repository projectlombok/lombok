import lombok.experimental.SuperBuilder;

public class SuperBuilderInAnonymousClass {
	Object annonymous = new Object() {
		@SuperBuilder
		class InnerParent {
			private String string;
		}

		@SuperBuilder
		class InnerChild {
			private String string;
		}
	};
}