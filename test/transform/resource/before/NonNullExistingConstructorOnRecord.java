// version 14:

import lombok.NonNull;

public record NonNullExistingConstructorOnRecord(@NonNull String a, @NonNull String b) {
	public NonNullExistingConstructorOnRecord(@NonNull String b) {
		this("default", b);
	}
}