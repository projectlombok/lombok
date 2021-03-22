// version 14:
import lombok.NonNull;
record NonNullOnRecord(@NonNull String a, @NonNull String b) {
	public void method(@NonNull String param) {
		if (param == null) {
			throw new java.lang.NullPointerException("param is marked non-null but is null");
		}
		String asd = "a";
	}
	@java.lang.SuppressWarnings("all")
	public NonNullOnRecord(@NonNull final String a, @NonNull final String b) {
		if (a == null) {
			throw new java.lang.NullPointerException("a is marked non-null but is null");
		}
		if (b == null) {
			throw new java.lang.NullPointerException("b is marked non-null but is null");
		}
		this.a = a;
		this.b = b;
	}
}
