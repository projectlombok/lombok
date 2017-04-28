
import java.io.IOException;

class SafeCallInStatements {
	int[] empty = {};

	public SafeCallInStatements() {
		int i4;
		{
			SafeCallInStatements i43 = nullObject();
			java.lang.Integer i42 = i43 != null ? i43.nullIndex() : null;
			int i44 = i42 != null ? i42 : 0;
			int[] i45 = empty;
			int i41 = i45 != null && i44 >= 0 && i44 < i45.length ? i45[i44] : 0;
			i4 = i41;
		}
		if (i4 == 0) {
			int i41;
			{
				int[] i412 = intNullArray();
				int i411 = i412 != null && 0 < i412.length ? i412[0] : 0;
				i41 = i411;
			}
		} else {
			int i42;
			{
				int[] i422 = intNullArray();
				int i421 = i422 != null && 0 < i422.length ? i422[0] : 0;
				i42 = i421;
			}
		}
		for (int i2 = -1; i2 < 0; i2++) {
			int i3;
			{
				int[] i32 = empty;
				int i31 = i32 != null && 0 < i32.length ? i32[0] : 0;
				i3 = i31;
			}
		}
		for (Object o : java.util.Arrays.asList(null, null)) {
			int i3;
			{
				int[] i32 = empty;
				int i31 = i32 != null && 0 < i32.length ? i32[0] : 0;
				i3 = i31;
			}
		}
		Integer i5 = 0;
		while (i5 > 0) {
			int i;
			{
				int[] i2 = new int[] {};
				int i1 = i2 != null && 0 < i2.length ? i2[0] : 0;
				i = i1;
			}
		}
		int i6 = 0;
		do {
			int i;
			{
				java.lang.Integer[] i2 = IntegerNullArray();
				java.lang.Integer i1 = i2 != null && 0 < i2.length ? i2[0] : null;
				i = i1 != null ? i1 : 0;
			}
		} while (i6 < 0);
		try {
			int i7;
			{
				java.lang.Integer[] i72 = IntegerNullArray();
				java.lang.Integer i71 = i72 != null && 0 < i72.length ? i72[0] : null;
				i7 = i71 != null ? i71 : 0;
			}
		} catch (Exception e) {
			int i7;
			{
				java.lang.Integer[] i72 = IntegerNullArray();
				java.lang.Integer i71 = i72 != null && 0 < i72.length ? i72[0] : null;
				i7 = i71 != null ? i71 : 0;
			}
		} finally {
			int i7;
			{
				java.lang.Integer[] i72 = IntegerNullArray();
				java.lang.Integer i71 = i72 != null && 0 < i72.length ? i72[0] : null;
				i7 = i71 != null ? i71 : 0;
			}
		}
		try {
			int i7;
			{
				java.lang.Integer[] i72 = IntegerNullArray();
				java.lang.Integer i71 = i72 != null && 0 < i72.length ? i72[0] : null;
				i7 = i71 != null ? i71 : 0;
			}
		} finally {
			int i7;
			{
				java.lang.Integer[] i72 = IntegerNullArray();
				java.lang.Integer i71 = i72 != null && 0 < i72.length ? i72[0] : null;
				i7 = i71 != null ? i71 : 0;
			}
		}
		int i7;
		{
			SafeCallInStatements i72 = nullObject();
			java.lang.Integer i71 = i72 != null ? i72.nullIndex() : null;
			i7 = i71 != null ? i71 : 0;
		}
		switch (i7) {
		case 1: 
			int i8;
			{
				int[] i82 = intNullArray();
				int i81 = i82 != null && 0 < i82.length ? i82[0] : 0;
				i8 = i81;
			}
			break;

		default: 
			int i9;
			{
				java.lang.Integer[] i92 = IntegerNullArray();
				java.lang.Integer i91 = i92 != null && 0 < i92.length ? i92[0] : null;
				i9 = i91 != null ? i91 : 0;
			}
			break;
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

	public SafeCallInStatements nullObject() {
		return null;
	}
}