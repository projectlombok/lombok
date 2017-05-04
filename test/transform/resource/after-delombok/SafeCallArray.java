
class SafeCallArray {
	int[] empty = {};

	public SafeCallArray() {
		int i4;
		{
			SafeCallArray i44 = nullSafeCallArray();
			java.lang.Integer i43 = i44 != null ? i44.nullIndex() : null;
			int i42 = -i43;
			int[] i45 = empty;
			int i41 = i45 != null && i42 >= 0 && i42 < i45.length ? i45[i42] : 0;
			i4 = i41;
		}
		int i;
		{
			int[] i2 = intNullArray();
			int i1 = i2 != null && 0 < i2.length ? i2[0] : 0;
			i = i1;
		}
		int i2;
		{
			int[] i22 = intEmptyArray();
			int i21 = i22 != null && 1 >= 0 && 1 < i22.length ? i22[1] : 0;
			i2 = i21;
		}
		int i3;
		{
			int[] i32 = empty;
			int i31 = i32 != null && 0 < i32.length ? i32[0] : 0;
			i3 = i31;
		}
		Integer i5;
		{
			java.lang.Integer[] i52 = IntegerNullArray();
			java.lang.Integer i51 = i52 != null && 0 < i52.length ? i52[0] : null;
			i5 = i51;
		}
		int i6;
		{
			int[] i64 = empty;
			int i63 = i64 != null && 0 < i64.length ? i64[0] : 0;
			java.lang.Integer i65 = nullIndex();
			int i66 = i65 != null ? i65 : 0;
			int[] i62 = new int[] {i63, i66, 3};
			int i61 = i62 != null && -1 >= 0 && -1 < i62.length ? i62[-1] : 0;
			i6 = i61;
		}
		int[] iAr;
		{
			int[] iAr3 = empty;
			int iAr2 = iAr3 != null && 0 < iAr3.length ? iAr3[0] : 0;
			java.lang.Integer[] iAr5 = IntegerNullArray();
			java.lang.Integer iAr4 = iAr5 != null && 0 < iAr5.length ? iAr5[0] : null;
			int iAr6 = iAr4 != null ? iAr4 : 0;
			java.lang.Integer iAr7 = nullIndex();
			int iAr8 = iAr7 != null ? iAr7 : 0;
			java.lang.Integer iAr9 = (Integer) null;
			int iAr10 = iAr9 != null ? iAr9 : 0;
			int[] iAr1 = new int[] {iAr2, iAr6, iAr8, iAr10, 1};
			iAr = iAr1;
		}
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