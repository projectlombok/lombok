import lombok.experimental.SafeCall;

class SafeCallPrimitivesNullCast {

	public SafeCallPrimitivesNullCast() {

		@SafeCall byte b = (Byte)null;
		@SafeCall int i = (Integer)null;
		@SafeCall long l = (Long)null;
		@SafeCall short s = (Short)null;
		@SafeCall float f = ((Float)null);
		@SafeCall double d = (Double)(null);
		@SafeCall char c = (((Character)((null))));
		@SafeCall boolean bool = (Boolean)null;
	}

}