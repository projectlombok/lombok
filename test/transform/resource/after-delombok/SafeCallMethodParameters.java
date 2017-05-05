
class SafeCallMethodParameters {
	public SafeCallMethodParameters() {
		int i;
		{
			java.lang.Integer i4 = nullIndex();
			int i5 = i4 != null ? i4 : 0;
			java.lang.Integer[] i7 = IntegerNullArray();
			java.lang.Integer i6 = i7 != null && 1 >= 0 && 1 < i7.length ? i7[1] : null;
			int i8 = i6 != null ? i6 : 0;
			SafeCallMethodParameters i3 = nullObject(i5, i8, 2);
			int[] i2 = i3 != null ? i3.intNullArray() : null;
			int i1 = i2 != null && 0 < i2.length ? i2[0] : 0;
			i = i1;
		}
	}

	public int[] intNullArray() {
		return null;
	}

	public Integer[] IntegerNullArray() {
		return null;
	}

	public Integer nullIndex() {
		return null;
	}

	public SafeCallMethodParameters nullObject(int i1, int... dots) {
		return null;
	}
}
