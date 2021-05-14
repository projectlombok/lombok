import java.util.Map;
import java.util.HashMap;

public class ValAnonymousSubclassSelfReference {
	public <T> void test(T arg) {
		T d = arg;
		Integer[] e = new Integer[1];
		int[] f = new int[0];
		java.util.Map<java.lang.String, Integer> g = new HashMap<String, Integer>();
		Integer h = 0;
		int i = 0;
		final int j = 1;
		final int k = 2;
		new ValAnonymousSubclassSelfReference() {
		};
	}
}