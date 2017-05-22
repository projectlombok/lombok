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
		@SafeCall int listElem1 = integers.size() ? listElem1 : defaultint;
		@SafeCall int listElem2 = integers.get(1) ? listElem2 : -1;
		
		final String defaultString = String.valueOf(defaultInteger);
		List<? extends CharSequence> charSequences = Arrays.<CharSequence>asList("", null, new StringBuilder());
		@SafeCall CharSequence listElem3 = charSequences.get(0) ? listElem3 : "";
		@SafeCall CharSequence listElem4 = charSequences.get(0) ? listElem4 : defaultString;
		
		List<SomeEum> enums = Arrays.<SomeEum>asList(SomeEum.first, SomeEum.second);
		@SafeCall SomeEum listElem5 = enums.get(0) ? listElem5 : SomeEum.third;
		
	}
	
}