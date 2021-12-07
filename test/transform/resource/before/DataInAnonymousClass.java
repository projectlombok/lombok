import lombok.Data;

public class DataInAnonymousClass {
	Object annonymous = new Object() {
		@Data
		class Inner {
			private String string;
		}
	};
}