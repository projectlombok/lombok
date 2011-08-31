

import lombok.*;

public class E1Getter0 {
	@Getter
	static final int target = 0;
	
	public void someMethod(){
		/* 1:ExtractMethod(anotherMethod):1*/
		System.out.println(target);
		/*:1:*/
	}
}