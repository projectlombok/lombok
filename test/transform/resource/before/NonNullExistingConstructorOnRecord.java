// version 14:

import lombok.NonNull;

record NonNullOnRecord(@NonNull String a, @NonNull String b) {
	public NonNullOnRecord(@NonNull String b) {
		this("default", b);
	}
}