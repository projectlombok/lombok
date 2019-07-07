import lombok.experimental.SafeCall;

class SafeCallMethodParameters {

	public SafeCallMethodParameters() {
		@SafeCall int i = nullObject(nullIndex(), IntegerNullArray()[1], 2).intNullArray()[0];
		@SafeCall int[] ia = nullObject2(nullIndex(), IntegerNullArray()[1]).intNullArray();
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
	public SafeCallMethodParameters nullObject2(int i1, int i2) {
		return null;
	}
}