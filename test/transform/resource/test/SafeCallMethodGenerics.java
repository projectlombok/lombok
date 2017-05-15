import lombok.experimental.SafeCall;

import java.util.*;

class SafeCallMethodGenerics {

	public SafeCallMethodGenerics() {
		String s = this.<String>tArg();
		@SafeCall List<? extends CharSequence> wildCardList = Arrays.<String>asList(s).subList(0, 1);
		@SafeCall CharSequence cs = wildCardList.get(0);

		@SafeCall List<CharSequence> list = Arrays.<CharSequence>asList(s).subList(0, 1);
		@SafeCall CharSequence cs2 = list.get(0);
	}

	public <T> T tArg() {
		return null;
	}

}