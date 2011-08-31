
import lombok.*;

public class E1Delegate0 {
	@Delegate
	public String someString = "abcdef";
	public void someMethod(){
	/* 1:ExtractMethod(anotherMethod):1*/
		Object someObject = someString;
		System.out.println(someString);
		someObject = 9;
		System.out.println(someObject);
		/*:1:*/
	} 
}