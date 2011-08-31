package I3;

import lombok.Getter;

public class I3Getter0 {

	@Getter int name = 1;
	
	public void someMethod() {
		/*1: InlineMethod(anotherMethod) :1*/
		String oldName = anotherMethod();
		/*:1:*/
		System.out.println(oldName);
	}
	public String anotherMethod(){
		String oldName="inlined";
		return oldName;
	}
}
