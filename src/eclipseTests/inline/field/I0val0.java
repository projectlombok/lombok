package inline.inlineField;



import java.util.HashMap;

import lombok.*;

public class I0val0 {
	public int target = 0;
	/*1: InlineField(target) :1*/
	public int method() {
	/*:1:*/
		val map = new HashMap<Integer, Integer>();
		map.put(0, target);
		for (val entry : map.entrySet()) {
			System.out.printf("%d: %s\n", entry.getKey(), entry.getValue());
		}
		return 0;
	}
}