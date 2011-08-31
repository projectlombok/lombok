

import lombok.*;

public class E1LazyGetter0 {
	@Getter(lazy=true)
	private static final int target = 0;
	
	public void someMethod(){
		/* 1:ExtractMethod(anotherMethod):1*/
		System.out.println(target);
		/*:1:*/
	}
}