

import lombok.Data;
@Data
public class E1Data0 {
	static final int target = 0;
	int someField = target;
	int anotherField;
	
	public void someMethod(){
		/* 1:ExtractMethod(anotherMethod):1*/
		System.out.println(target);
		someField = 9;
		/*:1:*/
	}
}