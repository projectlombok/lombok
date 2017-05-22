import lombok.experimental.SafeCall;

import java.util.Arrays;
import java.util.List;

class SafeCallDefaultValue {
	enum SomeEum {
		first, second, third;
	}
	
	{
		final Integer defaultInteger = (int) System.nanoTime();
		final int defaultint = -1;
		List<Integer> integers = Arrays.<Integer>asList(1, 2, 3);
		@SafeCall int wrongRef = integers.size() ? defaultint : defaultint;
		@SafeCall int wrongFalseType = integers.get(1) ? wrontFalseType : "";
		@SafeCall int wrongFalseUnboxing = integers.get(1) ? wrongFalseUnboxing : defaultInteger;
		@SafeCall int wrongConditionType = integers.get(1) > 0 ? wrongConditionType : -1;
		
		@SafeCall int wrongFalseType = integers.get(1) ? wrontFalseType : "";
		
		final String defaultString = String.valueOf(defaultInteger);
		List<? extends CharSequence> charSequences = Arrays.<CharSequence>asList("", null, new StringBuilder());
		@SafeCall CharSequence listElem3 = charSequences.get(0) ? listElem3 : "";
		@SafeCall CharSequence listElem4 = charSequences.get(0) ? listElem4 : defaultString;
		
		List<SomeEum> enums = Arrays.<SomeEum>asList(SomeEum.first, SomeEum.second);
		@SafeCall SomeEum listElem5 = enums.get(0) ? listElem5 : SomeEum.third;
		
	}
	
}