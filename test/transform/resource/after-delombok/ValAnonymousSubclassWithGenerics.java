import java.util.*;
public class ValAnonymousSubclassWithGenerics {
	Object object = new Object() {
		void foo() {
			final int j = 1;
		}
	};
	void bar() {
		final int k = super.hashCode();
		int x = k;
	}
	java.util.List<String> names = new java.util.ArrayList<String>() {
		public String get(int i) {
			final java.lang.String result = super.get(i);
			return result;
		}
	};
}
