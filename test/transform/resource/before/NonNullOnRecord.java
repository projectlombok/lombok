// version 14:

import lombok.NonNull;

record NonNullOnRecord(@NonNull String a, @NonNull String b) {
	public void method(@NonNull String param) {
		String asd = "a";
	}
}