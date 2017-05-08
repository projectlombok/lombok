
class SafeCallOperators {
	public SafeCallOperators() {
		Boolean negation;
		{
			java.lang.Boolean negation2 = Boolean();
			boolean negation3 = negation2 != null ? negation2 : false;
			boolean negation1 = !negation3;
			negation = negation1;
		}
		Integer nullInt = null;
		int minus;
		{
			java.lang.Integer minus2 = nullInt;
			int minus3 = minus2 != null ? minus2 : 0;
			int minus1 = -minus3;
			minus = minus1;
		}
		int increase;
		{
			java.lang.Integer increase2 = nullInt;
			int increase3 = increase2 != null ? increase2 : 0;
			int increase1 = increase3++;
			increase = increase1;
		}
		int addition = nullInt + nullInt;
		Integer assignResult = null;
		int assignment = assignResult = nullInt;
		int compoundAssignment = assignResult += nullInt;
		int condition = nullInt == 0 ? nullInt + 1 : nullInt;
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