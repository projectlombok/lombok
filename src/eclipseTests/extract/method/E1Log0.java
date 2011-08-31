

import lombok.extern.java.Log;
@Log
public class E1Log0 {
	
	static final int target = 0;
	public int someMethod() {
		/* 1:ExtractMethod(anotherMethod):1*/
		log.warning("Oh, no");
		return target;
		/*:1:*/
	}
}