import lombok.experimental.SafeCall;

class SafeCallMethodParameters {

	public SafeCallMethodParameters() {
		@SafeCall int i = nullObject(nullIndex(), IntegerNullArray()[1], 2).intNullArray()[0];
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