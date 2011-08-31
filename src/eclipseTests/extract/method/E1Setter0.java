

import lombok.*;

public class E1Setter0 {
	
	static final int target = 0;
	@Setter
	int someField = target;
	public void someMethod(){
		/* 1:ExtractMethod(anotherMethod):1*/
		System.out.println(target);
		someField = 9;
		/*:1:*/
	}
}