import lombok.experimental.SafeCall;

class SafeCallArray {
	int[] empty = {};

	public SafeCallArray() {
		@SafeCall int i4 = empty[-nullSafeCallArray().nullIndex()];
		@SafeCall int i = intNullArray()[0];
		@SafeCall int i2 = intEmptyArray()[1];
		@SafeCall int i3 = empty[0];
		@SafeCall Integer i5 = IntegerNullArray()[0];
		@SafeCall int i6 = new int[]{empty[0], nullIndex(), 3}[-1];
		@SafeCall int[] iAr = new int[]{empty[0], IntegerNullArray()[0], nullIndex(), (Integer) null, 1};
		@SafeCall int[][][] iAr2 = new int[nullIndex()][-1][];
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