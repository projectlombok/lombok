import lombok.experimental.SafeCall;

class SafeCallOperators {

	public SafeCallOperators() {

		@SafeCall Boolean negation = !Boolean();
		Integer nullInt = null;
		@SafeCall int minus = -nullInt;
		@SafeCall int increase = nullInt++;
		@SafeCall int addition = nullInt + nullInt;
		Integer assignResult = null;
		@SafeCall int assignment = assignResult = nullInt;
		@SafeCall int compoundAssignment = assignResult += nullInt;
		@SafeCall int condition = nullInt == 0 ?
				nullInt + 1:
				nullInt;

	}

	public String getNullString() {
		return null;
	}

	public Integer Integer() {
		return null;
	}
	public Boolean Boolean() {
		return null;
	}

	public SafeCallOperators nullObject() {
		return null;
	}
}