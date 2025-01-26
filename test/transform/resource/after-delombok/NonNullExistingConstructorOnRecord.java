// version 16:
import lombok.NonNull;
public record NonNullExistingConstructorOnRecord(@NonNull String a, @NonNull String b) {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullExistingConstructorOnRecord {
		if (a == null) {
			throw new java.lang.NullPointerException("a is marked non-null but is null");
		}
		if (b == null) {
			throw new java.lang.NullPointerException("b is marked non-null but is null");
		}
	}
	public NonNullExistingConstructorOnRecord(@NonNull String b) {
		this("default", b);
		if (b == null) {
			throw new java.lang.NullPointerException("b is marked non-null but is null");
		}
	}
}
