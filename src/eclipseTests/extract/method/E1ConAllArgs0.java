

import lombok.*;
@RequiredArgsConstructor
public class E1ConAllArgs0 {
	int someField = 0;
	int anotherField;
	
	public void someMethod(){
		/* 1:ExtractMethod(anotherMethod):1*/
		System.out.println(someField);
		/*:1:*/
	}
}