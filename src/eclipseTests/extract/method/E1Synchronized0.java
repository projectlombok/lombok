

import lombok.*;
public class E1Synchronized0 {
	
	static final int target = 0;
	
	@Synchronized
	public int method(){
		/* 1:ExtractMethod(anotherMethod):1*/
		return (Integer)target;
		/*:1:*/
	}
}