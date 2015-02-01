// issue 205: val inside anonymous inner classes is a bit tricky in javac, this test ensures we don't break it.
import java.util.*;
import lombok.val;

public class ValAnonymousSubclassWithGenerics {
	Object object = new Object(){
		void foo() {
			val j = 1;
		}
	};
	
	void bar() {
		val k = super.hashCode();
		int x = k;
	}
	
	java.util.List<String> names = new java.util.ArrayList<String>() {
		public String get(int i) {
			val result = super.get(i);
			return result;
		}
	};
}
