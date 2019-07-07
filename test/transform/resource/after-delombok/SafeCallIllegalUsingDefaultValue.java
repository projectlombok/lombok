
import java.util.Arrays;
import java.util.List;

class SafeCallIllegalUsingDefaultValue {
	static SafeCallIllegalUsingDefaultValue staticObj;
	static int[] staticIntArray;
	SafeCallIllegalUsingDefaultValue obj;
	int nonStaticField;
	
	{
		final Integer defaultInteger = (int) System.nanoTime();
		final int defaultint = -1;
		List<Integer> integers = Arrays.<Integer>asList(1, 2, 3);
		int wrongRef = integers.size();
		int wrongRefType = integers.size();
		int wrongRefType2 = integers.size();
		int wrongFalseUnboxing = integers.get(1);
		int falseNonStatic = integers.get(1);
		int falseNonStatic2 = integers.get(1);
		int falseIsMethodCall = integers.get(1);
		int falseFieldAccess = integers.get(1);
		int wrongConditionType = integers.get(1) > 0;
	}
	
	int inMethod() {
		return 0;
	}
}