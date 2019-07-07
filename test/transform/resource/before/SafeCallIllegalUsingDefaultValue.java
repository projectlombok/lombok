import lombok.experimental.SafeCall;

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
		@SafeCall int wrongRef = integers.size() ? defaultint : defaultint;
		@SafeCall int wrongRefType = integers.size() ? obj.nonStaticField : defaultint;
		@SafeCall int wrongRefType2 = integers.size() ? inMethod() : defaultint;
		@SafeCall int wrongFalseUnboxing = integers.get(1) ? wrongFalseUnboxing : defaultInteger;
		@SafeCall int falseNonStatic = integers.get(1) ? falseNonStatic : obj.nonStaticField;
		@SafeCall int falseNonStatic2 = integers.get(1) ? falseNonStatic2 : staticObj.nonStaticField;
		@SafeCall int falseIsMethodCall = integers.get(1) ? falseIsMethodCall : inMethod();
		@SafeCall int falseFieldAccess = integers.get(1) ? falseFieldAccess : staticIntArray.length;
		@SafeCall int wrongConditionType = integers.get(1) > 0 ? wrongConditionType : -1;
	}
	
	int inMethod() {
		return 0;
	}
	
}