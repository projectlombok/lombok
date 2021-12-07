// version :9
// issue 2420: to trigger the problem 2 var/val, at least one normal variable and a anonymous self reference is required
import java.util.Map;
import java.util.HashMap;

import lombok.val;

public class ValAnonymousSubclassSelfReference {
	public <T> void test(T arg) {
		T d = arg;
		Integer[] e = new Integer[1];
		int[] f = new int[0];
		java.util.Map<java.lang.String, Integer> g = new HashMap<String, Integer>();
		Integer h = 0;  
		int i = 0;
		
		val j = 1;
		val k = 2;

		new ValAnonymousSubclassSelfReference() { };
	}
}