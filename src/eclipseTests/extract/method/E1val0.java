
import java.util.HashMap;

import lombok.*;

public class E1val0 {
	public int target = 0;
	
	public int someMethod() {
		/*1: ExtractMethod(anotherMethod) :1*/
		val map = new HashMap<Integer, Integer>();
		map.put(0, target);
		for (val entry : map.entrySet()) {
			System.out.printf("%d: %s\n", entry.getKey(), entry.getValue());
		}
		/*:1:*/
		return 0;
	}
}