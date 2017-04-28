import lombok.experimental.SafeCall;

class SafeCallPrimitives {

	public SafeCallPrimitives() {

		@SafeCall int i = newSafeCallPrimitives().Integer().intValue();
		@SafeCall int i2 = Integer();
		@SafeCall int i3 = (int)getNullString().length();
		@SafeCall byte bl = (byte)(long)Integer();
		@SafeCall long lb = Integer().byteValue();
		@SafeCall long l = (long)(getNullString().length());
		@SafeCall float f = (float)(getNullString().length());
		@SafeCall double d = (double)(getNullString().length());
		@SafeCall char c = getNullString().charAt(0);
		@SafeCall boolean bool = getNullString().isEmpty();
	}

	public String getNullString() {
		return null;
	}
	public Integer Integer() {
		return null;
	}
	public SafeCallPrimitives newSafeCallPrimitives() {
		return null;
	}
}