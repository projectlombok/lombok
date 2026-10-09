// version 16:
import lombok.NonNull;
public record NonNullOnRecordTypeUse(@NonNull int[] a, int @NonNull [] b, int[] @NonNull [] c) {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullOnRecordTypeUse {
		if (b == null) {
			throw new java.lang.NullPointerException("b is marked non-null but is null");
		}
	}
}
