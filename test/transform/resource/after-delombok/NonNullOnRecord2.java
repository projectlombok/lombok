// version 16:
import lombok.NonNull;
record NonNullOnRecord2(@NonNull String a) {
	public NonNullOnRecord2 {
		if (a == null) {
			throw new java.lang.NullPointerException("a is marked non-null but is null");
		}
		System.out.println("Hello");
	}
}
