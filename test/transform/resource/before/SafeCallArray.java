import lombok.experimental.SafeCall;

class SafeCallArray {
	int[] empty = {};
	public SafeCallArray() {
		@SafeCall int i4 = empty[nullSafeCallArray().nullIndex()];
		@SafeCall int i = intNullArray()[0];
		@SafeCall int i2 = intEmptyArray()[1];
		@SafeCall int i3 = empty[0];
		@SafeCall Integer i5 = IntegerNullArray()[0];
		@SafeCall int i6 = new int[]{}[0];

	}

	public int[] intNullArray() {
		return null;
	}
	public Integer[] IntegerNullArray() {
		return null;
	}
	public int[] intEmptyArray() {
		return empty;
	}
	public Integer nullIndex() {
		return null;
	}

	public SafeCallArray nullSafeCallArray() {
		return null;
	}
}