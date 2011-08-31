

import lombok.*;
@EqualsAndHashCode
public class E1EqualsHash0 {
	
	static final int target = 0;
	int someField = target;
	public void someMethod(){
		/* 1:ExtractMethod(anotherMethod):1*/
		System.out.println(target);
		someField = 9;
		/*:1:*/
	}
	
}