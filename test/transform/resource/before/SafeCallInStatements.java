import lombok.experimental.SafeCall;

import java.io.IOException;

class SafeCallInStatements {
	int[] empty = {};

	public SafeCallInStatements() {
		@SafeCall int i4 = empty[nullObject().nullIndex()];
		if (i4 == 0) {
			@SafeCall int i41 = intNullArray()[0];
		} else {
			@SafeCall int i42 = intNullArray()[0];
		}

		for (int i2 = -1; i2 < 0; i2++) {
			@SafeCall int i3 = empty[0];
		}

		for (Object o: java.util.Arrays.asList(null, null)) {
			@SafeCall int i3 = empty[0];
		}

		@SafeCall Integer i5 = 0;
		while (i5 > 0) {
			@SafeCall int i = new int[]{}[0];
		}

		@SafeCall int i6 = 0;
		do {
			@SafeCall int i = IntegerNullArray()[0];
		} while (i6 < 0);

		try {
			@SafeCall int i7 = IntegerNullArray()[0];
		} catch (Exception e) {
			@SafeCall int i7 = IntegerNullArray()[0];
		} finally {
			@SafeCall int i7 = IntegerNullArray()[0];
		}

		try {
			@SafeCall int i7 = IntegerNullArray()[0];
		} finally {
			@SafeCall int i7 = IntegerNullArray()[0];
		}

		@SafeCall int i7 = nullObject().nullIndex();
		switch (i7) {
			case 1:
				@SafeCall int i8 = intNullArray()[0];
				break;
			default:
				@SafeCall int i9 = IntegerNullArray()[0];
				break;
		}

		synchronized (empty) {
			@SafeCall int i10 = IntegerNullArray()[0];
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