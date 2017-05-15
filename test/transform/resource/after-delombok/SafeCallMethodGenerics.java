
import java.util.*;

class SafeCallMethodGenerics {
	public SafeCallMethodGenerics() {
		String s = this.<String>tArg();
		List<? extends CharSequence> wildCardList;
		{
			java.lang.String wildCardList3 = s;
			java.util.List<java.lang.String> wildCardList2 = java.util.Arrays.<String>asList(wildCardList3);
			java.util.List<java.lang.String> wildCardList1 = wildCardList2 != null ? wildCardList2.subList(0, 1) : null;
			wildCardList = wildCardList1;
		}
		CharSequence cs;
		{
			java.util.List<? extends .java.lang.CharSequence> cs2 = wildCardList;
			java.lang.CharSequence cs1 = cs2 != null ? cs2.get(0) : null;
			cs = cs1;
		}
		List<CharSequence> list;
		{
			java.lang.String list3 = s;
			java.util.List<java.lang.CharSequence> list2 = java.util.Arrays.<CharSequence>asList(list3);
			java.util.List<java.lang.CharSequence> list1 = list2 != null ? list2.subList(0, 1) : null;
			list = list1;
		}
		CharSequence cs2;
		{
			java.util.List<java.lang.CharSequence> cs22 = list;
			java.lang.CharSequence cs21 = cs22 != null ? cs22.get(0) : null;
			cs2 = cs21;
		}
	}

	public <T> T tArg() {
		return null;
	}
}