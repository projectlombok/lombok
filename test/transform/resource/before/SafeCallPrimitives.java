import lombok.experimental.SafeCall;

class SafeCallPrimitives {

	public SafeCallPrimitives() {

		@SafeCall int i = newSafeCallPrimitives(newSafeCallPrimitives(getNullArg1()).getNullArg2()).Integer().intValue();
		@SafeCall int i2 = Integer();
		@SafeCall int i3 = (int) getNullString().length();
		@SafeCall Integer i4 = (int) Integer();
		@SafeCall byte bl = (byte) (long) Integer();
		@SafeCall long lb = Integer().byteValue();
		@SafeCall long l = (long) (getNullString().length());
		@SafeCall long l2 = (long) getNullString((String) getNullObject()).length();
		@SafeCall float f = (float) (getNullString().length());
		@SafeCall double d = (double) (getNullString().length());
		@SafeCall char c = getNullString().charAt(0);
		@SafeCall boolean bool = getNullString().isEmpty();
	}

	public String getNullString() {
		return null;
	}

	public String getNullString(String arg1) {
		return null;
	}

	public Object getNullObject() {
		return null;
	}

	public Integer Integer() {
		return null;
	}

	public SafeCallPrimitives newSafeCallPrimitives(Object arg) {
		return null;
	}

	public static String getNullString(Object arg) {
		return null;
	}

	public static Integer getNullArg1() {
		return null;
	}

	public static Integer getNullArg2() {
		return null;
	}
}