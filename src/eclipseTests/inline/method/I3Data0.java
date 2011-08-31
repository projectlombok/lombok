package I3;

import lombok.Data;

@Data
public class I3Data0 {
	int oldName;
	public void someMethod() {
		/*1: InlineMethod(anotherMethod) :1*/
		int oldName = anotherMethod();
		/*:1:*/
		System.out.println(oldName);
	}
	public int anotherMethod(){
return 5;
	}

}
