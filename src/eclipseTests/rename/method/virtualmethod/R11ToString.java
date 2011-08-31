package R11;

import lombok.ToString;

@ToString
public class R11ToString {

	int field = 0;
	
	/*1: RenameVirtualMethod(newMethodName) :1*/
	public String method(){
		return "The value of my field is " + field;
	}
	/*:1:*/
	
}
