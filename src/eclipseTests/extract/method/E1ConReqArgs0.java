

import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class E1ConReqArgs0 {
	public final int someField = 0;
	int anotherField;
	
	public void someMethod(){
		/* 1:ExtractMethod(anotherMethod):1*/
		System.out.println(someField);
		anotherField = 9;
		/*:1:*/
	}
}